package cern.ais.gridwars.web.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Entity
public class User implements UserDetails {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final GrantedAuthority ROLE_USER_AUTHORITY = new SimpleGrantedAuthority(ROLE_USER);
    public static final GrantedAuthority ROLE_ADMIN_AUTHORITY = new SimpleGrantedAuthority(ROLE_ADMIN);

    @Transient
    private final List<GrantedAuthority> authorities = new LinkedList<>();

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    @NotNull
    @NotEmpty
    @Size(min = 4, max = 16)
    @Pattern(regexp = "[A-Za-z0-9-_]+")
    private String username;

    @Column(nullable = false)
    @Size(min = 6, max = 512) // This field stores a hash, so make it big enough!
    private String password;

    @Column(nullable = false, unique = true)
    @NotNull
    @NotEmpty
    @Size(min = 4, max = 32)
    private String teamName;

    @Column(nullable = false, unique = true)
    @NotNull
    @NotEmpty
    // TODO Validate email
    @Size(max = 32)
    private String email;

    @Column(nullable = false)
    private Instant created;

    @Column(unique = true)
    @Size(max = 256)
    private String confirmationId;

    private Instant confirmed;

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

    public User setPassword(String password) {
        this.password = password;
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

    public User setCreated(Instant created) {
        this.created = created;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.unmodifiableList(authorities);
    }

    @Override
    public String getPassword() {
        return password;
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
        return enabled;
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
