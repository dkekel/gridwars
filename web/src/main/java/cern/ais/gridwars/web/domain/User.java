package cern.ais.gridwars.web.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Entity
public class User implements UserDetails {

    private static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    private static final String ROLE_USER = "ROLE_" + USER;
    private static final String ROLE_ADMIN = "ROLE_" + ADMIN;
    public static final GrantedAuthority ROLE_USER_AUTHORITY = new SimpleGrantedAuthority(ROLE_USER);
    public static final GrantedAuthority ROLE_ADMIN_AUTHORITY = new SimpleGrantedAuthority(ROLE_ADMIN);

    @Transient
    private final List<GrantedAuthority> authorities = new LinkedList<>();

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String teamName;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private Instant created;

    private Instant modified;

    @Column(unique = true)
    private String confirmationId;

    private Instant confirmed;

    private String ip;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean admin = false;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getTeamName() {
        return teamName;
    }

    public User setTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Instant getCreated() {
        return created;
    }

    public LocalDateTime getCreatedDateTime() {
        return LocalDateTime.ofInstant(created, ZoneId.systemDefault());
    }

    public User setCreated(Instant created) {
        this.created = created;
        return this;
    }

    public Instant getModified() {
        return modified;
    }

    public User setModified(Instant modified) {
        this.modified = modified;
        return this;
    }

    public String getConfirmationId() {
        return confirmationId;
    }

    public User setConfirmationId(String confirmationId) {
        this.confirmationId = confirmationId;
        return this;
    }

    public Instant getConfirmed() {
        return confirmed;
    }

    public User setConfirmed(Instant confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public LocalDateTime getConfirmedDateTime() {
        return (confirmed != null) ? LocalDateTime.ofInstant(confirmed, ZoneId.systemDefault()) : null;
    }

    public String getIp() {
        return ip;
    }

    public User setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public User setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public User setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public User setAuthorities(Collection<GrantedAuthority> authorities) {
        clearAuthorities();
        this.authorities.addAll(authorities);
        return this;
    }

    public User addAuthority(GrantedAuthority authority) {
        this.authorities.add(authority);
        return this;
    }

    public User clearAuthorities() {
        this.authorities.clear();
        return this;
    }

    public boolean isConfirmed() {
        return (confirmed != null);
    }

    public void touch() {
        setModified(Instant.now());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableList(authorities);
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled && isConfirmed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
