package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
            .findByUsername(username)
            .map(this::populateAuthorities)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private User populateAuthorities(User user) {
        if (user.isAdmin()) {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }

        return user;
    }

    @Transactional
    public void createNormalUser(User user) {
        user.setAdmin(false);
        saveUser(user);
    }

    @Transactional
    public void createAdminUser(User user) {
        user.setAdmin(true);
        saveUser(user);
    }

    private void saveUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateKeyException("User already exists: " + user.getUsername());
        }

        user.setId(generateId());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);

        userRepository.save(user);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
