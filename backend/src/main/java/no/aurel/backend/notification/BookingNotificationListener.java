package no.aurel.backend.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.BookingConfirmedEvent;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.customer.PhoneNumber;

// When a booking is confirmed: a text message to the customer and an e-mail with all details to Agneta.
// AFTER_COMMIT: nothing is sent for a booking that failed to save. @Async: the customer does not wait for Twilio,
// and a failing text message can never undo or block a booking (it is only logged).
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(BookingNotificationListener.class);

    private final SmsSender smsSender;
    private final EmailSender emailSender;
    private final MessageTemplateService messageTemplates;

    // Agneta's e-mail address for the "new booking" mail. Empty = she gets no e-mail.
    @Value("${app.email.admin-address:}")
    private String adminEmail;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        AppointmentResponse appointment = event.appointment();
        send(appointment.customerPhone(), messageTemplates.confirmationFor(appointment));
        mailAdmin(appointment);
    }

    private void mailAdmin(AppointmentResponse appointment) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }
        try {
            emailSender.send(adminEmail, BookingMessages.adminSubject(appointment), BookingMessages.forAdmin(appointment));
        } catch (RuntimeException e) {
            log.warn("Could not e-mail booking to {}: {}", adminEmail, e.getMessage());
        }
    }

    private void send(String rawPhone, String text) {
        String phone = PhoneNumber.toE164(rawPhone);
        if (phone == null) {
            if (rawPhone != null && !rawPhone.isBlank()) {
                log.warn("Skipped text message: '{}' is not a usable phone number", rawPhone);
            }
            return;
        }
        try {
            smsSender.send(phone, text);
        } catch (RuntimeException e) {
            log.warn("Could not send text message to {}: {}", phone, e.getMessage());
        }
    }
}
