package no.aurel.backend.notification;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;

@ExtendWith(MockitoExtension.class)
class ReminderJobTest {

    private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private SmsSender smsSender;

    private static AppointmentResponse appointment(String phone) {
        return new AppointmentResponse(7L, 2L, "Kari Nordmann", phone, 3L, "Agneta",
                List.of(4L), List.of("Gellack"), List.of(), 750,
                LocalDateTime.of(2026, 10, 16, 14, 0), LocalDateTime.of(2026, 10, 16, 15, 30),
                null, "token", PaymentMethod.AT_SALON, PaymentStatus.UNPAID, AppointmentStatus.BOOKED);
    }

    private ReminderJob jobAt(int hour) {
        Clock clock = Clock.fixed(LocalDateTime.of(2026, 10, 15, hour, 0).atZone(OSLO).toInstant(), OSLO);
        return new ReminderJob(appointmentService, smsSender, clock);
    }

    @Test
    void textsCustomerAndMarksAppointmentAsReminded() {
        when(appointmentService.findDueForReminder()).thenReturn(List.of(appointment("912 34 567")));

        jobAt(12).sendReminders();

        verify(smsSender).send("+4791234567", BookingMessages.reminder(appointment("912 34 567")));
        verify(appointmentService).markReminderSent(7L);
    }

    @Test
    void doesNothingAtNight() {
        jobAt(3).sendReminders();

        verifyNoInteractions(appointmentService, smsSender);
    }

    @Test
    void failedTextIsNotMarkedSoItIsRetried() {
        when(appointmentService.findDueForReminder()).thenReturn(List.of(appointment("912 34 567")));
        doThrow(new SmsException("Twilio is down", null)).when(smsSender).send(anyString(), anyString());

        jobAt(12).sendReminders();

        verify(appointmentService, never()).markReminderSent(7L);
    }

    @Test
    void unusablePhoneNumberIsMarkedWithoutTexting() {
        when(appointmentService.findDueForReminder()).thenReturn(List.of(appointment("abc")));

        jobAt(12).sendReminders();

        verify(smsSender, never()).send(anyString(), anyString());
        verify(appointmentService).markReminderSent(7L);
    }
}
