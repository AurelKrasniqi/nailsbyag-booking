package no.aurel.backend.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;

class MessageTemplateRendererTest {

    private static AppointmentResponse appointment() {
        return new AppointmentResponse(1L, 2L, "Kari Nordmann", "912 34 567", 3L, "Agneta",
                List.of(4L, 5L), List.of("Gellack", "Pedikyr"), List.of(), 750,
                LocalDateTime.of(2026, 10, 15, 14, 0), LocalDateTime.of(2026, 10, 15, 15, 30),
                null, "token", PaymentMethod.AT_SALON, PaymentStatus.UNPAID, AppointmentStatus.BOOKED);
    }

    @Test
    void fillsInEveryPlaceholder() {
        String text = MessageTemplateRenderer.render(
                "Hei {navn}! {behandling} {dato} kl. {tid}, {pris}.", appointment());
        assertEquals("Hei Kari! Gellack, Pedikyr 15.10.2026 kl. 14:00, 750 kr.", text);
    }

    @Test
    void sameplaceholderCanBeUsedTwice() {
        assertEquals("14:00 14:00", MessageTemplateRenderer.render("{tid} {tid}", appointment()));
    }

    @Test
    void unknownPlaceholderIsLeftAsItIs() {
        assertEquals("Hei {dao}", MessageTemplateRenderer.render("Hei {dao}", appointment()));
    }

    @Test
    void findsUnknownPlaceholdersOnce() {
        assertEquals(List.of("dao", "foo"),
                MessageTemplateRenderer.unknownPlaceholders("{dato} {dao} {dao} {foo} {tid}"));
    }

    @Test
    void defaultConfirmationHasDateAndTimeAndFitsInOneSms() {
        String text = MessageTemplateRenderer.render(MessageTemplateService.DEFAULT_CONFIRMATION, appointment());
        assertEquals("Hei! Takk for at du booket time hos oss på thebeautyroombyag. "
                + "Dette er en bekreftelse på bookingen din 15.10.2026 kl. 14:00. Vi ses!", text);
        assertTrue(text.length() <= 160, "should fit in one SMS");
    }

    @Test
    void defaultReminderSaysTomorrowAndFitsInOneSms() {
        String text = MessageTemplateRenderer.render(MessageTemplateService.DEFAULT_REMINDER, appointment());
        assertTrue(text.contains("i morgen kl. 14:00"));
        assertTrue(text.contains("15.10.2026"));
        assertTrue(text.length() <= 160, "should fit in one SMS");
    }

    @Test
    void defaultsContainNoUnknownPlaceholders() {
        assertTrue(MessageTemplateRenderer.unknownPlaceholders(MessageTemplateService.DEFAULT_CONFIRMATION).isEmpty());
        assertTrue(MessageTemplateRenderer.unknownPlaceholders(MessageTemplateService.DEFAULT_REMINDER).isEmpty());
    }
}
