package no.aurel.backend.notification;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.aurel.backend.appointment.dto.AppointmentResponse;

// Fills the {placeholders} of a message text with the details of an appointment. Pure functions, easy to unit test.
public final class MessageTemplateRenderer {

    // The placeholders Agneta can use. The admin page shows this list as buttons.
    public static final List<String> PLACEHOLDERS = List.of("navn", "dato", "tid", "behandling", "pris");

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.of("nb", "NO"));
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.of("nb", "NO"));
    private static final Pattern TOKEN = Pattern.compile("\\{([^{}]*)}");

    private MessageTemplateRenderer() {
    }

    public static String render(String template, AppointmentResponse a) {
        Map<String, String> values = Map.of(
                "navn", firstName(a.customerName()),
                "dato", a.startTime().format(DATE),
                "tid", a.startTime().format(TIME),
                "behandling", String.join(", ", a.serviceNames()),
                "pris", a.totalPriceNok() + " kr");
        return TOKEN.matcher(template).replaceAll(match -> {
            String value = values.get(match.group(1));
            return Matcher.quoteReplacement(value != null ? value : match.group());
        });
    }

    // Placeholders that are not in PLACEHOLDERS, e.g. a typo like {dao}. They would be sent to the customer as-is.
    public static List<String> unknownPlaceholders(String template) {
        List<String> unknown = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(template);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!PLACEHOLDERS.contains(name) && !unknown.contains(name)) {
                unknown.add(name);
            }
        }
        return unknown;
    }

    private static String firstName(String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }
}
