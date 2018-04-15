package cern.ais.gridwars.web.service;

import cern.ais.gridwars.web.config.GridWarsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MailService {

    private static final String SUBJECT_PREFIX = "[GridWars] ";

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final JavaMailSender mailSender;
    private final GridWarsProperties gridWarsProperties;

    @Autowired
    public MailService(JavaMailSender mailSender, GridWarsProperties gridWarsProperties) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public void sendMail(MailBuilder mailBuilder) {
        if (mailSendingEnabled()) {
            sendMailAsync(createMailMessage(mailBuilder));
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
        message.setSubject(SUBJECT_PREFIX + mailBuilder.getSubject());

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
            return String.join(";", recipients);
        }
    }

    private void sendMailAsync(final SimpleMailMessage message) {
        executorService.execute(() -> doSend(message));
    }

    private void doSend(SimpleMailMessage message) {
        try {
            long startMillis = System.currentTimeMillis();
            mailSender.send(message);
            long durationMillis = System.currentTimeMillis() - startMillis;

            if (LOG.isInfoEnabled()) {
                LOG.info("Sent mail to: {}; subject: {}; took: {} ms", String.join(",", message.getTo()),
                    message.getSubject(), durationMillis);
            }
        } catch (Exception e) {
            LOG.error("Failed to send mail: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    protected void destroy() {
        executorService.shutdownNow();
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
