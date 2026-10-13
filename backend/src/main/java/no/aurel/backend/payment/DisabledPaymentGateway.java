package no.aurel.backend.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Used on the live site until real Vipps credentials exist (app.payment.provider=none).
// Online payment is simply turned off: the booking page hides the Vipps option and the backend refuses it.
// Unlike the fake gateway this can never "approve" anything, so it is safe in production.
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "none")
public class DisabledPaymentGateway implements PaymentGateway {

    private static final String OFF = "Online payment is turned off";

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public String createPayment(String reference, int amountNok, String description) {
        throw new IllegalStateException(OFF);
    }

    @Override
    public PaymentGatewayState getState(String reference) {
        throw new IllegalStateException(OFF);
    }

    @Override
    public void capture(String reference, int amountNok) {
        throw new IllegalStateException(OFF);
    }

    @Override
    public void cancel(String reference) {
        throw new IllegalStateException(OFF);
    }

    @Override
    public void refund(String reference, int amountNok) {
        throw new IllegalStateException(OFF);
    }
}
