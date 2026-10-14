package no.aurel.backend.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Development: "sends" the e-mail by writing it to the log. Active with app.email.provider=log.
@Component
@ConditionalOnProperty(name = "app.email.provider", havingValue = "log")
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String text) {
        log.info("E-mail to {}\nSubject: {}\n{}", to, subject, text);
    }
}
