package no.aurel.backend.notification;

import java.time.Clock;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.customer.PhoneNumber;

// Texts every customer the day before their appointment. It runs every 10 minutes instead of once a day at a fixed
// time: if the server was down or Twilio failed, the next run simply tries again. The appointment's reminder_sent_at
// is what stops a customer from getting the same text twice.
@Component
@RequiredArgsConstructor
public class ReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ReminderJob.class);

    // Nobody wants a reminder at 03:00: only send during the day
    private static final LocalTime FIRST_SEND_TIME = LocalTime.of(10, 0);
    private static final LocalTime LAST_SEND_TIME = LocalTime.of(20, 0);

    private final AppointmentService appointmentService;
    private final SmsSender smsSender;
    private final MessageTemplateService messageTemplates;
    private final Clock clock;

    @Scheduled(fixedDelay = 600_000, initialDelay = 60_000)
    public void sendReminders() {
        LocalTime now = LocalTime.now(clock);
        if (now.isBefore(FIRST_SEND_TIME) || now.isAfter(LAST_SEND_TIME)) {
            return;
        }
        for (AppointmentResponse appointment : appointmentService.findDueForReminder()) {
            try {
                remind(appointment);
            } catch (RuntimeException e) {
                // One failing text must not stop the others; it is retried on the next run
                log.warn("Could not send reminder for appointment {}: {}", appointment.id(), e.getMessage());
            }
        }
    }

    private void remind(AppointmentResponse appointment) {
        String phone = PhoneNumber.toE164(appointment.customerPhone());
        if (phone != null) {
            smsSender.send(phone, messageTemplates.reminderFor(appointment));
        } else {
            log.warn("No reminder for appointment {}: '{}' is not a usable phone number",
                    appointment.id(), appointment.customerPhone());
        }
        // Also marked when there is no usable number: retrying would never help
        appointmentService.markReminderSent(appointment.id());
    }
}
