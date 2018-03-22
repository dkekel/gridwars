package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class UserService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Mock implementation, replace with database
        if ("user1".equalsIgnoreCase(username) || "user2".equalsIgnoreCase(username)) {
            return createNormalUser(username);
        } else if ("admin".equalsIgnoreCase(username)) {
            return createAdminUser(username);
        } else {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }

    private User createNormalUser(String username) {
        return new User()
            .setUsername(username)
            .setPassword(passwordEncoder.encode("bla"))
            .setEmail(username + "@cern.ch")
            .setTeamname("Team " + username)
            .setAdmin(false)
            .setEnabled(true)
            .addAuthority(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private User createAdminUser(String username) {
        return new User()
            .setUsername(username)
            .setPassword(passwordEncoder.encode("blabla"))
            .setEmail(username + "@cern.ch")
            .setTeamname("Team " + username)
            .setAdmin(true)
            .setEnabled(true)
            .addAuthority(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
