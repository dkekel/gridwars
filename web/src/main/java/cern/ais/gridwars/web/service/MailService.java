package cern.ais.gridwars.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class MailService {

    private static final String FROM = "CERN GridWars <grid.wars@cern.ch>";
    private static final String BCC_RECIPIENT = "benjamin.wolff@cern.ch";

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = Objects.requireNonNull(mailSender);
    }

    public void sendMail(MailBuilder mailBuilder) {
        doSend(createMailMessage(mailBuilder));
    }

    private SimpleMailMessage createMailMessage(MailBuilder mailBuilder) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        message.setTo(mailBuilder.getTo());
        message.setCc(mailBuilder.getCc());
        message.setBcc(BCC_RECIPIENT);
        message.setSubject(mailBuilder.getSubject());
        message.setText(mailBuilder.getText());
        return message;
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
