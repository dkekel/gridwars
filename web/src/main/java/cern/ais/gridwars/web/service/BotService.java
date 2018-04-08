package cern.ais.gridwars.web.service;

import cern.ais.gridwars.bot.PlayerBot;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.BotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private final JarStorageService jarStorageService;

    @Autowired
    public BotService(BotRepository botRepository, JarStorageService jarStorageService) {
        this.botRepository = Objects.requireNonNull(botRepository);
        this.jarStorageService = Objects.requireNonNull(jarStorageService);
    }

    public void validateAndPersistBotJarFile(MultipartFile uploadedJarFile, User user, Instant uploadTime) {
        File storedBotJarFile = null;
        try {
            storedBotJarFile = storeBotJarFile(uploadedJarFile, user, uploadTime);
            validateBotJarFile(storedBotJarFile);
            persistBotJarFile(storedBotJarFile, user);
        } catch (Exception e) {
            LOG.error("Failed to validate bot jar uploaded by user \"{}\": {}", user.getUsername(), e.getMessage(), e);
            deleteBotJarFile(storedBotJarFile);
            throw e;
        }
    }

    private File storeBotJarFile(MultipartFile uploadedJarFile, User user, Instant uploadTime) {
        return jarStorageService.storeJarFile(uploadedJarFile, user.getId(), uploadTime);
    }

    private void validateBotJarFile(File botJarFile) {
        try (JarFile jarFile = new JarFile(botJarFile)) {
            String botClassName = extractBotClassName(jarFile);
            loadAndValidateBotClass(botClassName, botJarFile);
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

        return fullyQualifiedBotClassName;
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

    private void persistBotJarFile(File botFile, User user) {
        // TODO Implement...
    }

    private void deleteBotJarFile(File botJarFile) {
        if (botJarFile != null) {
            if (!botJarFile.delete()) {
                LOG.warn("Invalid bot jar file could not be deleted: {}", botJarFile.getAbsolutePath());
            }
        }
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
