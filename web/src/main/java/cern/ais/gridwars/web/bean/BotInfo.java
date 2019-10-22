package cern.ais.gridwars.web.bean;

import cern.ais.gridwars.web.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

public class BotInfo {
    private final Instant uploadTime;
    private User uploadUser;
    private String uploadIp;
    private MultipartFile botJarFile;
    private String botDescription;

    public BotInfo() {
        this.uploadTime = Instant.now();
    }

    public Instant getUploadTime() {
        return uploadTime;
    }

    public User getUploadUser() {
        return uploadUser;
    }

    public void setUploadUser(final User uploadUser) {
        this.uploadUser = uploadUser;
    }

    public String getUploadIp() {
        return uploadIp;
    }

    public void setUploadIp(final String uploadIp) {
        this.uploadIp = uploadIp;
    }

    public MultipartFile getBotJarFile() {
        return botJarFile;
    }

    public void setBotJarFile(final MultipartFile botJarFile) {
        this.botJarFile = botJarFile;
    }

    public String getBotDescription() {
        return botDescription;
    }

    public void setBotDescription(final String botDescription) {
        this.botDescription = botDescription;
    }
}
