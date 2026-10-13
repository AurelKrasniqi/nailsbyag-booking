package no.aurel.backend.payment;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// Endpoints behind the fake payment page. They only exist when the fake gateway is active.
@RestController
@RequestMapping("/api/dev/fake-vipps")
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "fake")
@RequiredArgsConstructor
public class FakeVippsController {

    private final FakePaymentGateway gateway;

    @PostMapping("/{reference}/approve")
    public ResponseEntity<Void> approve(@PathVariable String reference) {
        gateway.approve(reference);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reference}/reject")
    public ResponseEntity<Void> reject(@PathVariable String reference) {
        gateway.reject(reference);
        return ResponseEntity.noContent().build();
    }
}
