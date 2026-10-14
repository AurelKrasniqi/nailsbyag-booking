package no.aurel.backend.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// E-mail switched off (app.email.provider=none, also the default). Bookings work exactly as before.
@Component
@ConditionalOnProperty(name = "app.email.provider", havingValue = "none", matchIfMissing = true)
public class DisabledEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String text) {
        // Intentionally empty
    }
}
