package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MailService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final JavaMailSender mailSender;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public MailService(JavaMailSender mailSender, GridWarsProperties gridWarsProperties) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public void sendMail(MailBuilder mailBuilder) {
        if (mailSendingEnabled()) {
            doSend(createMailMessage(mailBuilder));
        } else {
            LOG.info("E-mail sending is disabled, discarding mail with subject: " + mailBuilder.getSubject());
        }
    }

    private boolean mailSendingEnabled() {
        return gridWarsProperties.getMail().getEnabled();
    }

    private SimpleMailMessage createMailMessage(MailBuilder mailBuilder) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(gridWarsProperties.getMail().getFrom());
        message.setSubject(mailBuilder.getSubject());

        String toRecipientOverride = gridWarsProperties.getMail().getToRecipientOverride();
        if (StringUtils.hasLength(toRecipientOverride)) {
            message.setText(mailBuilder.getText() +
                "\n\nOriginal TO: " + joinRecipients(mailBuilder.getTo()) +
                "\nOriginal CC: " + joinRecipients(mailBuilder.getCc()));
            message.setTo(toRecipientOverride);
        } else {
            message.setTo(mailBuilder.getTo());
            message.setCc(mailBuilder.getCc());
            message.setText(mailBuilder.getText());
        }

        String bccRecipient = gridWarsProperties.getMail().getBccRecipient();
        if (StringUtils.hasLength(bccRecipient)) {
            message.setBcc(bccRecipient);
        }

        return message;
    }

    private String joinRecipients(String[] recipients) {
        if ((recipients == null) || (recipients.length == 0)) {
            return "";
        } else {
            return Stream.of(recipients).collect(Collectors.joining(";"));
        }
    }

    private void doSend(SimpleMailMessage message) {
        mailSender.send(message);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sent mail to: {}; subject: {}", String.join(",", message.getTo()), message.getSubject());
        }
    }

    public static final class MailBuilder {
        private String[] to;
        private String[] cc;
        private String subject;
        private String text;

        public static MailBuilder newMail() {
            return new MailBuilder();
        }

        private MailBuilder() {
        }

        public String[] getTo() {
            return to;
        }

        public MailBuilder setTo(String... to) {
            this.to = to;
            return this;
        }

        public String[] getCc() {
            return cc;
        }

        public MailBuilder setCc(String... cc) {
            this.cc = cc;
            return this;
        }

        public String getSubject() {
            return subject;
        }

        public MailBuilder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public String getText() {
            return text;
        }

        public MailBuilder setText(String text) {
            this.text = text;
            return this;
        }
    }
}
