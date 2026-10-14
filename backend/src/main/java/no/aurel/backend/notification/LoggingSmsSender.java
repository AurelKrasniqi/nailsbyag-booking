package no.aurel.backend.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Development: "sends" the message by writing it to the log. Active with app.sms.provider=log.
@Component
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "log")
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String toE164, String text) {
        log.info("SMS to {}:\n{}", toE164, text);
    }
}
