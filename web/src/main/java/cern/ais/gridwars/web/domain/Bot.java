package cern.ais.gridwars.web.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "bot")
public class Bot {

    @Id
    private String id;

    @Column(nullable = false)
    @Size(max = 256) // Contains a UUID, so make it big enough!
    private String jarFileName;

    @Column(nullable = false)
    @Size(max = 64)
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

    public String getBotClassName() {
        return botClassName;
    }

    public Bot setBotClassName(String botClassName) {
        this.botClassName = botClassName;
        return this;
    }

    public String getShortBotClassName() {
        if ((botClassName != null) && !botClassName.isEmpty()) {
            String[] classNameParts = botClassName.split("\\.");
            return classNameParts[classNameParts.length - 1];
        } else {
            return botClassName;
        }
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
