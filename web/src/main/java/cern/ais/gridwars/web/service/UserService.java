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
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMailingService userMailingService;


    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                       UserMailingService userMailingService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.userMailingService = Objects.requireNonNull(userMailingService);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository
            .findByUsername(username)
            .map(this::populateAuthorities)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(String userId) {
        return userRepository.findById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> getByConfirmationId(String confirmationId) {
        return userRepository.findByConfirmationId(confirmationId);
    }

    @Transactional
    public void confirmUser(String userId) {
        getById(userId).ifPresent(user -> {
            user.setConfirmed(Instant.now());
            userRepository.saveAndFlush(user);
        });
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
    public User create(User newUser, boolean bypassConfirmation, boolean sendMailToAdmin) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new UserFieldValueException("username", "user.error.exists.username");
        }

        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new UserFieldValueException("email", "user.error.exists.email");
        }

        if (userRepository.existsByTeamName(newUser.getTeamName())) {
            throw new UserFieldValueException("teamName", "user.error.exists.teamName");
        }

        User newSavedUser = saveNewUser(newUser, bypassConfirmation);

        if (!bypassConfirmation) {
            userMailingService.sendConfirmationMail(newSavedUser);
        }

        if (sendMailToAdmin) {
            userMailingService.sendUserRegistrationMailToAdmin(newUser);
        }

        return newSavedUser;
    }

    private User saveNewUser(User user, boolean bypassConfirmation) {
        User newUser = new User()
            .setId(DomainUtils.generateId())
            .setUsername(user.getUsername())
            .setPassword(encodePassword(user.getPassword()))
            .setEmail(user.getEmail())
            .setCreated(Instant.now())
            .setTeamName(user.getTeamName())
            .setAdmin(user.isAdmin())
            .setConfirmationId(DomainUtils.generateId())
            .setIp(user.getIp())
            .setEnabled(true);

        if (bypassConfirmation) {
            newUser.setConfirmed(Instant.now());
        } else {
            newUser.setConfirmed(null);
        }

        userRepository.saveAndFlush(newUser);
        return newUser;
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

    private void updateExistingUser(User user) {
       userRepository.findById(user.getId()).ifPresent(existingUser -> {
           existingUser.setEmail(user.getEmail());
           existingUser.setTeamName(user.getTeamName());

           if (StringUtils.hasLength(user.getPassword())) {
               existingUser.setPassword(encodePassword(user.getPassword()));
           }

           userRepository.saveAndFlush(existingUser);
       });

       // TODO Throw exception if user not found??
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
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
