package cern.ais.gridwars.web.service;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.DomainUtils;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.BotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


@Service
public class BotService {

    private static final String BOT_CLASS_NAME_MANIFEST_HEADER = "Bot-Class-Name";

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BotRepository botRepository;
    private final MatchService matchService;
    private final JarStorageService jarStorageService;

    @Autowired
    public BotService(BotRepository botRepository, MatchService matchService, JarStorageService jarStorageService) {
        this.botRepository = Objects.requireNonNull(botRepository);
        this.matchService = Objects.requireNonNull(matchService);
        this.jarStorageService = Objects.requireNonNull(jarStorageService);
    }

    @Transactional
    public void validateAndCreateNewBot(MultipartFile uploadedBotJarFile, User user, Instant uploadTime) {
        File storedBotJarFile = null;
        try {
            storedBotJarFile = storeBotJarFile(uploadedBotJarFile, user, uploadTime);
            String botClassName = validateBotJarFileAndGetBotClassName(storedBotJarFile);
            createNewBot(storedBotJarFile, botClassName, user, uploadTime);
        } catch (Exception e) {
            LOG.error("Failed to validate and persist bot uploaded by user \"{}\": {}", user.getUsername(),
                e.getMessage(), e);
            FileUtils.deleteFile(storedBotJarFile);
            throw e;
        }
    }

    private File storeBotJarFile(MultipartFile uploadedJarFile, User user, Instant uploadTime) {
        return jarStorageService.storeJarFile(uploadedJarFile, user.getId(), uploadTime);
    }

    private String validateBotJarFileAndGetBotClassName(File botJarFile) {
        try (JarFile jarFile = new JarFile(botJarFile)) {
            String botClassName = extractBotClassName(jarFile);
            loadAndValidateBotClass(botClassName, botJarFile);
            return botClassName;
        } catch (IOException e) {
            throw new BotUploadException("Invalid jar file", e);
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
            throw new BotUploadException("Could not load META-INF/MANIFEST.MF file in bot jar file");
        }

        return manifest;
    }

    private String extractBotClassNameFromManifest(Manifest manifest) {
        String fullyQualifiedBotClassName = manifest.getMainAttributes().getValue(BOT_CLASS_NAME_MANIFEST_HEADER);

        if (fullyQualifiedBotClassName == null) {
            throw new BotUploadException("The META-INF/MANIFEST.MF file is missing the bot class name header: " + BOT_CLASS_NAME_MANIFEST_HEADER);
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
            throw new BotUploadException("Failed to instantiate bot class: " + botClassName, e);
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
            throw new BotUploadException("Bot class must not be an interface or abstract: " + botClass.getName());
        }

        if (!PlayerBot.class.isAssignableFrom(botClass)) {
            throw new BotUploadException("Bot class does not implement required interface: " + PlayerBot.class.getName());
        }
    }

    private void createNewBot(File botJarFile, String botClassName, User user, Instant uploadTime) {
        inactivateOldBot(user);
        Bot newBot = createNewBotRecord(botJarFile, botClassName, user, uploadTime);
        matchService.generateMatches(newBot);
    }

    private void inactivateOldBot(User user) {
        botRepository.findAllByUserAndActiveIsTrue(user).forEach(oldBot -> {
            matchService.cancelMatches(oldBot);
            oldBot.setActive(false);
            botRepository.saveAndFlush(oldBot);
        });
    }

    private Bot createNewBotRecord(File botFile, String botClassName, User user, Instant uploadTime) {
        Bot newBot = new Bot()
            .setId(DomainUtils.generateId())
            .setUser(user)
            .setJarName(botFile.getName())
            .setBotClassName(botClassName)
            .setUploaded(uploadTime)
            .setActive(true);

        botRepository.saveAndFlush(newBot);
        return newBot;
    }

    public static class BotUploadException extends RuntimeException {

        public BotUploadException(String message) {
            super(message);
        }

        public BotUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
