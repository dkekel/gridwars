package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;


@Service
public class BotFileService {

    private static final DateTimeFormatter INSTANT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public BotFileService(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    File storeUploadedJarFile(MultipartFile uploadedJarFile, String userId, Instant uploadTime) {
        File storedJarFile = createNewBotJarFile(userId, uploadTime);
        LOG.info("Storing uploaded bot jar file: {}", storedJarFile.getAbsolutePath());

        try {
            uploadedJarFile.transferTo(storedJarFile);
        } catch (IOException | IllegalStateException e) {
            FileUtils.deleteFile(storedJarFile);
            throw new RuntimeException("Failed to store uploaded bot jar file", e);
        }

        return storedJarFile;
    }

    private File createNewBotJarFile(String userId, Instant uploadTime) {
        return Paths.get(gridWarsProperties.getDirectories().getBotJarDir(), determineBotJarFileName(userId, uploadTime)).toFile();
    }

    private String determineBotJarFileName(String userId, Instant uploadTime) {
        return userId + "_" + formatInstant(uploadTime) + ".jar";
    }

    private String formatInstant(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(INSTANT_FORMATTER);
    }

    public Optional<File> getBotJarFile(Bot bot) {
        return Optional.of(createExistingBotJarFile(bot)).filter(FileUtils::existsAndHasContent);
    }

    private File createExistingBotJarFile(Bot bot) {
        return Paths.get(gridWarsProperties.getDirectories().getBotJarDir(), bot.getJarFileName()).toFile();
    }
}
