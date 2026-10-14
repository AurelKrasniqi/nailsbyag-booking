package no.aurel.backend.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.appointment.dto.AppointmentResponse.AddOnLine;

class BookingMessagesTest {

    private static AppointmentResponse appointment(String comment, List<AddOnLine> addOns,
            PaymentMethod method, PaymentStatus paymentStatus) {
        return new AppointmentResponse(1L, 2L, "Kari Nordmann", "912 34 567", 3L, "Agneta",
                List.of(4L), List.of("Gellack"), addOns, 750,
                LocalDateTime.of(2026, 10, 15, 14, 0), LocalDateTime.of(2026, 10, 15, 15, 30),
                comment, "token", method, paymentStatus, AppointmentStatus.BOOKED);
    }

    @Test
    void customerMessageHasDateAndTime() {
        String text = BookingMessages.forCustomer(
                appointment(null, List.of(), PaymentMethod.AT_SALON, PaymentStatus.UNPAID));
        assertEquals("Hei! Takk for at du booket time hos oss på thebeautyroombyag. "
                + "Dette er en bekreftelse på bookingen din 15.10.2026 kl. 14:00. Vi ses!", text);
        assertTrue(text.length() <= 160, "should fit in one SMS");
    }

    @Test
    void adminMessageHasEverythingAboutTheCustomer() {
        String text = BookingMessages.forAdmin(appointment("Lang og mandel",
                List.of(new AddOnLine("Fransk", 1, 100), new AddOnLine("Nailart", 4, 200)),
                PaymentMethod.VIPPS, PaymentStatus.PAID));
        assertTrue(text.startsWith("BOOKING 15.10.2026 kl. 14:00-15:30"));
        assertTrue(text.contains("Kari Nordmann, 912 34 567"));
        assertTrue(text.contains("Gellack"));
        assertTrue(text.contains("Tillegg: Fransk, 4 x Nailart"));
        assertTrue(text.contains("750 kr (betalt med Vipps)"));
        assertTrue(text.contains("Kommentar: Lang og mandel"));
    }

    @Test
    void adminMessageLeavesOutEmptyParts() {
        String text = BookingMessages.forAdmin(
                appointment(null, List.of(), PaymentMethod.AT_SALON, PaymentStatus.UNPAID));
        assertFalse(text.contains("Tillegg"));
        assertFalse(text.contains("Kommentar"));
        assertTrue(text.contains("betales i salongen"));
    }
}
