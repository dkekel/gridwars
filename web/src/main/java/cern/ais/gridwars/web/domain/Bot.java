package cern.ais.gridwars.web.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "bot")
public class Bot {

    @Id
    private String id;

    @Column(nullable = false)
    private String jarName;

    @Column(nullable = false)
    private String mainClassName;

    @Column(nullable = false)
    private Integer sizeInKb;

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

    public String getJarName() {
        return jarName;
    }

    public Bot setJarName(String jarName) {
        this.jarName = jarName;
        return this;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public Bot setMainClassName(String mainClassName) {
        this.mainClassName = mainClassName;
        return this;
    }

    public Integer getSizeInKb() {
        return sizeInKb;
    }

    public Bot setSizeInKb(Integer sizeInKb) {
        this.sizeInKb = sizeInKb;
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
