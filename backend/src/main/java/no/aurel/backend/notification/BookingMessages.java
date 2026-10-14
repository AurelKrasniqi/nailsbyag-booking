package no.aurel.backend.notification;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;

// The wording of the two text messages. Pure functions of the appointment, so they are easy to unit test.
// No emoji on purpose: one emoji switches an SMS to a encoding with 70 instead of 160 characters per message.
public final class BookingMessages {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.of("nb", "NO"));
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.of("nb", "NO"));

    private BookingMessages() {
    }

    public static String forCustomer(AppointmentResponse a) {
        return "Hei! Takk for at du booket time hos oss på thebeautyroombyag. "
                + "Dette er en bekreftelse på bookingen din " + a.startTime().format(DATE)
                + " kl. " + a.startTime().format(TIME) + ". Vi ses!";
    }

    public static String adminSubject(AppointmentResponse a) {
        return "Ny booking " + a.startTime().format(DATE) + " kl. " + a.startTime().format(TIME)
                + " - " + a.customerName();
    }

    public static String forAdmin(AppointmentResponse a) {
        List<String> lines = new ArrayList<>();
        lines.add("BOOKING " + a.startTime().format(DATE) + " kl. " + a.startTime().format(TIME)
                + "-" + a.endTime().format(TIME));
        lines.add("Kunde: " + a.customerName() + ", " + a.customerPhone());
        lines.add("Behandling: " + String.join(", ", a.serviceNames()));
        if (!a.addOns().isEmpty()) {
            lines.add("Tillegg: " + a.addOns().stream()
                    .map(x -> x.quantity() > 1 ? x.quantity() + " x " + x.name() : x.name())
                    .reduce((x, y) -> x + ", " + y).orElse(""));
        }
        lines.add("Pris: " + a.totalPriceNok() + " kr (" + payment(a) + ")");
        if (a.comment() != null) {
            lines.add("Kommentar: " + a.comment());
        }
        return String.join("\n", lines);
    }

    private static String payment(AppointmentResponse a) {
        if (a.paymentMethod() == PaymentMethod.VIPPS && a.paymentStatus() == PaymentStatus.PAID) {
            return "betalt med Vipps";
        }
        return "betales i salongen";
    }
}
