package no.aurel.backend.notification;

public class SmsException extends RuntimeException {

    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
