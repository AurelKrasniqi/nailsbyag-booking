package no.aurel.backend.notification;

// Customers type their number any way they like ("912 34 567", "+47 912 34 567"); Twilio needs +4791234567.
public final class PhoneNumber {

    private PhoneNumber() {
    }

    // Returns null when the input does not look like a usable number, so the caller can skip the text message
    public static String toE164(String input) {
        if (input == null) {
            return null;
        }
        String digits = input.replaceAll("[\\s\\-().]", "");
        if (digits.startsWith("00")) {
            digits = "+" + digits.substring(2);
        }
        if (digits.matches("\\d{8}")) {
            return "+47" + digits; // a plain Norwegian number
        }
        if (digits.matches("47\\d{8}")) {
            return "+" + digits;
        }
        if (digits.matches("\\+\\d{8,15}")) {
            return digits;
        }
        return null;
    }
}
