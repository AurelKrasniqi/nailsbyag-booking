package no.aurel.backend.payment;

// The payment provider could not be reached or answered with an error
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
