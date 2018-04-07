package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository
            .findByUsername(username)
            .map(this::populateAuthorities)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private User populateAuthorities(final User user) {
        user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        if (user.isAdmin()) {
            user.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        return user;
    }

    @Transactional
    public void create(final User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new FieldValueAlreadyExistsException("username", "user.error.exists.username");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new FieldValueAlreadyExistsException("email", "user.error.exists.email");
        }

        if (userRepository.existsByTeamname(user.getTeamname())) {
            throw new FieldValueAlreadyExistsException("teamname", "user.error.exists.teamname");
        }

        saveNewUser(user);
    }

    @Transactional
    public void update(final User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User does not exist: " + user.getId() + " (" + user.getUsername() + ")");
        }

        if (userRepository.existsByUsernameAndIdNot(user.getUsername(), user.getId())) {
            throw new FieldValueAlreadyExistsException("username", "user.error.exists.username");
        }

        if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new FieldValueAlreadyExistsException("email", "user.error.exists.email");
        }

        if (userRepository.existsByTeamnameAndIdNot(user.getTeamname(), user.getId())) {
            throw new FieldValueAlreadyExistsException("teamname", "user.error.exists.teamname");
        }

        updateExistingUser(user);
    }

    private void saveNewUser(final User user) {
        user.setId(generateId());
        user.setPassword(encodePassword(user.getPassword()));

        userRepository.save(user);
    }

    private void updateExistingUser(final User user) {
       userRepository.findById(user.getId()).ifPresent(existingUser -> {
           existingUser.setEmail(user.getEmail());
           existingUser.setTeamname(user.getTeamname());

           if (isNotBlank(user.getPassword())) {
               existingUser.setPassword(encodePassword(user.getPassword()));
           }

           userRepository.save(existingUser);
       });
    }

    private String encodePassword(final String password) {
        return passwordEncoder.encode(password);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private boolean isNotBlank(final String value) {
        return (value != null) && !value.isEmpty();
    }

    public static class FieldValueAlreadyExistsException extends RuntimeException {

        private final String fieldName;
        private final String errorMessageCode;

        public FieldValueAlreadyExistsException(final String fieldName, final String errorMessageCode) {
            super();
            this.fieldName = fieldName;
            this.errorMessageCode = errorMessageCode;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getErrorMessageCode() {
            return errorMessageCode;
        }
    }
}
