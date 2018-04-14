package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.util.DomainUtils;
import cern.ais.gridwars.web.domain.User;
import cern.ais.gridwars.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        user.clearAuthorities();
        user.addAuthority(User.ROLE_USER_AUTHORITY);

        if (user.isAdmin()) {
            user.addAuthority(User.ROLE_ADMIN_AUTHORITY);
        }

        return user;
    }

    @Transactional
    public User create(User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new UserFieldValueException("username", "user.error.exists.username");
        }

        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new UserFieldValueException("email", "user.error.exists.email");
        }

        if (userRepository.existsByTeamName(newUser.getTeamName())) {
            throw new UserFieldValueException("teamName", "user.error.exists.teamName");
        }

        return saveNewUser(newUser);
    }

    @Transactional
    public void update(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User does not exist: " + user.getId() + " (" + user.getUsername() + ")");
        }

        if (userRepository.existsByUsernameAndIdNot(user.getUsername(), user.getId())) {
            throw new UserFieldValueException("username", "user.error.exists.username");
        }

        if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new UserFieldValueException("email", "user.error.exists.email");
        }

        if (userRepository.existsByTeamNameAndIdNot(user.getTeamName(), user.getId())) {
            throw new UserFieldValueException("teamName", "user.error.exists.teamName");
        }

        updateExistingUser(user);
    }

    private User saveNewUser(User user) {
        User newUser = new User()
            .setId(DomainUtils.generateId())
            .setUsername(user.getUsername())
            .setPassword(encodePassword(user.getPassword()))
            .setEmail(user.getEmail())
            .setCreated(Instant.now())
            .setTeamName(user.getTeamName())
            .setAdmin(user.isAdmin())
            .setEnabled(true) // TODO disable by default and send confirmation mail
            .setConfirmationId(DomainUtils.generateId())
            .setConfirmed(Instant.now()); // TODO should be set to null (if current user is not an admin) and must be confirmed by the user...

        userRepository.saveAndFlush(newUser);
        return newUser;
    }

    private void updateExistingUser(User user) {
       userRepository.findById(user.getId()).ifPresent(existingUser -> {
           existingUser.setEmail(user.getEmail());
           existingUser.setTeamName(user.getTeamName());

           if (isNotBlank(user.getPassword())) {
               existingUser.setPassword(encodePassword(user.getPassword()));
           }

           userRepository.saveAndFlush(existingUser);
       });
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean isNotBlank(String value) {
        return (value != null) && !value.isEmpty();
    }

    public static class UserFieldValueException extends RuntimeException {

        private final String fieldName;
        private final String errorMessageCode;

        UserFieldValueException(String fieldName, String errorMessageCode) {
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
