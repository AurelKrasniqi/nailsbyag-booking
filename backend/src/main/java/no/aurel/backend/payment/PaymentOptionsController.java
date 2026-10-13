package no.aurel.backend.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// Public: the booking page asks which ways of paying it may offer
@RestController
@RequestMapping("/api/payment-options")
@RequiredArgsConstructor
public class PaymentOptionsController {

    private final PaymentService paymentService;

    @GetMapping
    public PaymentOptions options() {
        return new PaymentOptions(paymentService.onlinePaymentEnabled());
    }

    public record PaymentOptions(boolean vippsEnabled) {
    }
}
