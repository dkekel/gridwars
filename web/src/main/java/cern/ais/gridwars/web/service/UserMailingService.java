package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserMailingService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final MailService mailService;
    private final GridWarsProperties gridWarsProperties;

    public UserMailingService(MailService mailService, GridWarsProperties gridWarsProperties) {
        this.mailService = Objects.requireNonNull(mailService);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public void sendConfirmationMail(User newUser) {
        try {
            mailService.sendMail(
                MailService.MailBuilder.newMail()
                    .setTo(newUser.getEmail())
                    .setSubject("User Confirmation")
                    .setText(createMailText(newUser))
            );
        } catch (Exception e) {
            LOG.error("Failed to send confirmation mail for user \"{}\": {}", newUser.getId(), e.getMessage(), e);
        }
    }

    private String createMailText(User user) {
        return String.format(
            "Hello %s,\n\n" +
            "Please visit the following link to confirm your user account: %s\n\n" +
            "Have fun and play fair :)\n\n" +
            "Best regards,\n" +
            "The GridWars Team\n",
            user.getUsername(), createConfirmationLink(user));
    }

    private String createConfirmationLink(User user) {
        return gridWarsProperties.getMail().getBaseUrl() + "/user/confirm/" + user.getConfirmationId();
    }

    public void sendUserRegistrationMailToAdmin(User newUser) {
        try {
            mailService.sendMail(
                MailService.MailBuilder.newMail()
                    .setTo(gridWarsProperties.getMail().getBccRecipient())
                    .setSubject("New User")
                    .setText(createNewUserMailText(newUser))
            );
        } catch (Exception e) {
            LOG.error("Failed to send admin registration mail for user \"{}\": {}", newUser.getId(), e.getMessage(), e);
        }
    }

    private String createNewUserMailText(User newUser) {
        return String.format(
            "Username: %s\n" +
            "Team Name: %s\n" +
            "Mail: %s\n",
            newUser.getUsername(), newUser.getTeamName(), newUser.getEmail()
        );
    }
}
