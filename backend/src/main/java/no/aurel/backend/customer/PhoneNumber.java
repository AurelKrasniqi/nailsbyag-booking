package no.aurel.backend.customer;

// Customers type their number any way they like ("912 34 567", "+47 912 34 567"); Twilio needs +4791234567.
public final class PhoneNumber {

    private PhoneNumber() {
    }

    // Returns null when the input is not a number we can send a text message to
    public static String toE164(String input) {
        if (input == null) {
            return null;
        }
        String digits = input.replaceAll("[\\s\\-().]", "");
        if (digits.startsWith("00")) {
            digits = "+" + digits.substring(2);
        }
        if (digits.matches("\\d{8}")) {
            digits = "+47" + digits; // a plain Norwegian number
        } else if (digits.matches("47\\d{8}")) {
            digits = "+" + digits;
        }
        if (!digits.matches("\\+\\d{8,15}")) {
            return null;
        }
        // Norwegian mobile numbers start with 4 or 9. Landlines (2, 3, 5, 6, 7) cannot receive a text message.
        if (digits.startsWith("+47") && !digits.matches("\\+47[49]\\d{7}")) {
            return null;
        }
        return digits;
    }

    public static boolean isValid(String input) {
        return toE164(input) != null;
    }
}
