package no.aurel.backend.notification;

// Sends one text message. The rest of the app only knows this interface, so the provider (Twilio, a log line, ...)
// can be swapped through app.sms.provider without touching the booking code.
public interface SmsSender {

    // toE164 is a full international number such as +4791234567
    void send(String toE164, String text);
}
