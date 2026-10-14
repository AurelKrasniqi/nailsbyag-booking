package no.aurel.backend.notification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Text messages switched off (app.sms.provider=none, also the default). Bookings work exactly as before.
@Component
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "none", matchIfMissing = true)
public class DisabledSmsSender implements SmsSender {

    @Override
    public void send(String toE164, String text) {
        // Intentionally empty
    }
}
