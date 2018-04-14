package cern.ais.gridwars.web.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;


@Entity
public class Bot {

    @Id
    private String id;

    @Column(nullable = false)
    @Size(max = 255)
    private String jarFileName;

    @Column(nullable = false)
    private Long jarFileSize;

    @Column(nullable = false)
    @Size(max = 255)
    private String jarFileHash;

    @Column(nullable = false)
    @Size(max = 63)
    private String botClassName;

    @Column(nullable = false)
    private Instant uploaded;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(optional = false)
    private User user;

    public String getId() {
        return id;
    }

    public Bot setId(String id) {
        this.id = id;
        return this;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public Bot setJarFileName(String jarFileName) {
        this.jarFileName = jarFileName;
        return this;
    }

    public Long getJarFileSize() {
        return jarFileSize;
    }

    public Bot setJarFileSize(Long jarFileSize) {
        this.jarFileSize = jarFileSize;
        return this;
    }

    public String getJarFileHash() {
        return jarFileHash;
    }

    public Bot setJarFileHash(String jarFileHash) {
        this.jarFileHash = jarFileHash;
        return this;
    }

    public String getBotClassName() {
        return botClassName;
    }

    public Bot setBotClassName(String botClassName) {
        this.botClassName = botClassName;
        return this;
    }

    public Instant getUploaded() {
        return uploaded;
    }

    public Bot setUploaded(Instant uploaded) {
        this.uploaded = uploaded;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Bot setActive(boolean active) {
        this.active = active;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Bot setUser(User user) {
        this.user = user;
        return this;
    }

    public boolean isAdminBot() {
        return getUser().isAdmin();
    }


    public String getName() {
        if ((botClassName != null) && !botClassName.isEmpty()) {
            String[] classNameParts = botClassName.split("\\.");
            return classNameParts[classNameParts.length - 1];
        } else {
            return botClassName;
        }
    }

    public String getTeamBotLabel() {
        return user.getTeamName() + " (" + getName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bot bot = (Bot) o;
        return Objects.equals(id, bot.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
