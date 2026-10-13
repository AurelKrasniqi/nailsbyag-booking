package no.aurel.backend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.AppointmentService;

// Releases times that were held for an online payment that never finished (customer closed the tab, etc.)
@Component
@RequiredArgsConstructor
public class PaymentExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpiryJob.class);

    private final AppointmentService appointmentService;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 30_000)
    public void releaseUnpaidBookings() {
        for (String token : appointmentService.findExpiredPendingTokens()) {
            try {
                paymentService.expire(token);
            } catch (RuntimeException e) {
                // One failing booking must not stop the others; it is retried on the next run
                log.warn("Could not process expired booking {}: {}", token, e.getMessage());
            }
        }
    }
}
