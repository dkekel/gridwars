package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class UserService implements UserDetailsService {

    private final List<User> users = new LinkedList<>();
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User createNormalUser(User user) {
        user.setAdmin(false);
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        saveUser(user);
        return user;
    }

    public User createAdminUser(User user) {
        user.setAdmin(true);
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        saveUser(user);
        return user;
    }

    private Optional<User> findByUsername(String username) {
        return users.stream()
            .filter(user -> user.getUsername().equalsIgnoreCase(username))
            .findAny();
    }

    private void saveUser(User user) {
        if (findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists: " + user.getUsername());
        }

        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        users.add(user);
    }
}
