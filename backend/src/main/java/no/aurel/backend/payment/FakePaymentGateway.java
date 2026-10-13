package no.aurel.backend.payment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Stand-in for Vipps while developing: the "payment page" is a page in our own frontend where you press
// approve or reject. Only active with app.payment.provider=fake. It is deliberately NOT the default:
// if it ran in production, anyone could "pay" without paying.
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "fake")
public class FakePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(FakePaymentGateway.class);

    // Lost on restart, which is fine for development
    private final Map<String, PaymentGatewayState> states = new ConcurrentHashMap<>();

    @Value("${app.payment.frontend-base-url}")
    private String frontendBaseUrl;

    @Override
    public String createPayment(String reference, int amountNok, String description) {
        states.put(reference, PaymentGatewayState.PENDING);
        log.info("Fake payment created: {} ({} kr)", reference, amountNok);
        return frontendBaseUrl + "/#/fake-vipps/" + reference;
    }

    @Override
    public PaymentGatewayState getState(String reference) {
        return states.getOrDefault(reference, PaymentGatewayState.PENDING);
    }

    @Override
    public void capture(String reference, int amountNok) {
        states.put(reference, PaymentGatewayState.CAPTURED);
    }

    @Override
    public void cancel(String reference) {
        states.put(reference, PaymentGatewayState.FAILED);
    }

    @Override
    public void refund(String reference, int amountNok) {
        log.info("Fake refund: {} ({} kr)", reference, amountNok);
    }

    // The two buttons on the fake payment page
    void approve(String reference) {
        states.computeIfPresent(reference,
                (ref, state) -> state == PaymentGatewayState.PENDING ? PaymentGatewayState.AUTHORIZED : state);
    }

    void reject(String reference) {
        states.computeIfPresent(reference,
                (ref, state) -> state == PaymentGatewayState.PENDING ? PaymentGatewayState.FAILED : state);
    }
}
