package no.aurel.backend.payment;

// What the rest of the application needs from a payment provider. The business logic only knows this interface,
// so the real Vipps implementation can be swapped for a fake one while developing without Vipps credentials.
public interface PaymentGateway {

    // False when online payment is switched off (provider "none"): customers can then only pay at the salon
    default boolean enabled() {
        return true;
    }

    /**
     * Starts a payment.
     *
     * @param reference our own unique id for the payment (the booking's public token)
     * @return the address the customer's browser must be sent to in order to pay
     */
    String createPayment(String reference, int amountNok, String description);

    PaymentGatewayState getState(String reference);

    // Takes the reserved money
    void capture(String reference, int amountNok);

    // Abandons a payment that has not been captured
    void cancel(String reference);

    // Sends captured money back to the customer
    void refund(String reference, int amountNok);
}
