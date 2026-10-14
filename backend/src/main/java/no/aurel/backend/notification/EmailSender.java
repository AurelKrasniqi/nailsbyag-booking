package no.aurel.backend.notification;

// Sends one plain-text e-mail. Same idea as SmsSender: the booking code does not know which provider is behind it.
public interface EmailSender {

    void send(String to, String subject, String text);
}
