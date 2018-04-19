package cern.ais.gridwars.web.service;

import cern.ais.gridwars.api.bot.PlayerBot;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.util.DomainUtils;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.BotRepository;
import cern.ais.gridwars.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;


@Service
public class BotService {

    private static final String BOT_CLASS_NAME_MANIFEST_HEADER = "Bot-Class-Name";

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BotRepository botRepository;
    private final BotFileService botFileService;

    @Autowired
    public BotService(BotRepository botRepository, BotFileService botFileService) {
        this.botRepository = Objects.requireNonNull(botRepository);
        this.botFileService = Objects.requireNonNull(botFileService);
    }

    @Transactional(readOnly = true)
    public Optional<Bot> getBotById(String botId) {
        return botRepository.findById(botId);
    }

    @Transactional(readOnly = true)
    public Optional<Bot> getActiveBotOfUser(User user) {
        return botRepository.findFirstByUserAndActiveIsTrueOrderByUploadedDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Bot> getAllActiveBots() {
        return botRepository.findAllByActiveIsTrue();
    }

    @Transactional(readOnly = true)
    public List<Bot> getAllActiveBotsOfUser(User user) {
        // Should usually only return a single result. Only returns a list in case there are multiple active bots,
        // for whatever reason.
        return botRepository.findAllByUserAndActiveIsTrueOrderByUploadedDesc(user);
    }

    @Transactional
    public Bot validateAndCreateNewUploadedBot(MultipartFile uploadedBotJarFile, User user, Instant uploadTime) {
        File storedBotJarFile = null;
        try {
            storedBotJarFile = storeUploadedBotJarFile(uploadedBotJarFile, user, uploadTime);
            String botClassName = validateBotJarFileAndExtractBotClassName(storedBotJarFile);
            return createNewBotRecord(storedBotJarFile, botClassName, user, uploadTime);
        } catch (Exception e) {
            LOG.error("Failed to validate and persist bot uploaded by user '{}': {}", user.getUsername(), e.getMessage());
            FileUtils.deleteFile(storedBotJarFile);
            throw e;
        }
    }

    private File storeUploadedBotJarFile(MultipartFile uploadedJarFile, User user, Instant uploadTime) {
        return botFileService.storeUploadedJarFile(uploadedJarFile, user.getId(), uploadTime);
    }

    private String validateBotJarFileAndExtractBotClassName(File botJarFile) {
        try (JarFile jarFile = new JarFile(botJarFile)) {
            String botClassName = extractBotClassName(jarFile);
            loadAndValidateBotClass(botClassName, botJarFile);
            return botClassName;
        } catch (IOException e) {
            throw new BotException("Invalid jar file", e);
        }
    }

    private String extractBotClassName(JarFile botJarFile) {
        Manifest manifest = loadManifest(botJarFile);
        String botClassName = extractBotClassNameFromManifest(manifest);
        LOG.debug("Extracted bot class name: {}", botClassName);
        return botClassName;
    }

    private Manifest loadManifest(JarFile botJarFile) {
        Manifest manifest = null;

        try {
            manifest =  botJarFile.getManifest();
        } catch (IOException ignored) {
        }

        if (manifest == null) {
            throw new BotException("Could not load META-INF/MANIFEST.MF file in bot jar file");
        }

        return manifest;
    }

    private String extractBotClassNameFromManifest(Manifest manifest) {
        String fullyQualifiedBotClassName = manifest.getMainAttributes().getValue(BOT_CLASS_NAME_MANIFEST_HEADER);

        if (fullyQualifiedBotClassName == null) {
            throw new BotException("The META-INF/MANIFEST.MF file is missing the bot class name header: " + BOT_CLASS_NAME_MANIFEST_HEADER);
        }

        return fullyQualifiedBotClassName.trim();
    }

    private void loadAndValidateBotClass(String botClassName, File botJarFile) {
        Class botClass = loadBotClass(botClassName, botJarFile);
        validateBotClass(botClass);
    }

    private Class loadBotClass(String botClassName, File botJarFile) {
        URL botJarFileUrl = toUrl(botJarFile);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { botJarFileUrl }, getClass().getClassLoader())) {
            return Class.forName(botClassName, false, classLoader);
        } catch (Exception e) {
            throw new BotException("Failed to instantiate bot class: " + botClassName, e);
        }
    }

    private URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateBotClass(Class botClass) {
        if (botClass.isInterface() || Modifier.isAbstract(botClass.getModifiers())) {
            throw new BotException("Bot class '" + botClass.getName() + "' must not be an interface or abstract");
        }

        if (!PlayerBot.class.isAssignableFrom(botClass)) {
            throw new BotException("Bot class '" + botClass.getName() + "' does not implement the required interface: " + PlayerBot.class.getName());
        }

        if (Stream.of(botClass.getConstructors()).noneMatch(constructor -> constructor.getParameterCount() == 0)) {
            throw new BotException("Bot class '" + botClass.getName() + "' does not define a parameter-less default constructor");
        }
    }

    @Transactional
    public Bot createNewBotRecord(File jarFile, String botClassName, User user, Instant uploadTime) {
        Bot newBot = new Bot()
            .setId(DomainUtils.generateId())
            .setUser(user)
            .setJarFileName(jarFile.getName())
            .setJarFileHash(getFileContentHash(jarFile))
            .setJarFileSize(jarFile.length())
            .setBotClassName(botClassName)
            .setUploaded(uploadTime)
            .setActive(true);

        botRepository.saveAndFlush(newBot);
        return newBot;
    }

    private String getFileContentHash(File file) {
        try {
            return Base64Utils.encodeToUrlSafeString(
                MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()))
            );
        } catch (Exception e) {
            LOG.warn("Failed to create SHA hash of file: " + file.getAbsolutePath(), e);
            return "<error>";
        }
    }

    @Transactional
    public void inactivateBot(Bot bot) {
        bot.setActive(false);
        bot.setInactivated(Instant.now());
        botRepository.saveAndFlush(bot);
    }

    public static class BotException extends RuntimeException {

        BotException(String message) {
            super(message);
        }

        BotException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
