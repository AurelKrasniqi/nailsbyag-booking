package no.aurel.backend.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;

// The payment rules: what happens to a booking depending on what the payment provider says.
// Deliberately not @Transactional: each step calls AppointmentService (which has its own short transaction),
// so no database transaction is held open while waiting for the payment provider.
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String DESCRIPTION = "Time hos The Beauty Room by AG";

    private final PaymentGateway gateway;
    private final AppointmentService appointmentService;

    // Whether customers may pay online at all (false when the provider is "none")
    public boolean onlinePaymentEnabled() {
        return gateway.enabled();
    }

    // Returns where the customer must be sent to pay
    public String start(AppointmentResponse appointment) {
        return gateway.createPayment(appointment.publicToken(), appointment.totalPriceNok(), DESCRIPTION);
    }

    /**
     * Called when the customer returns from the payment page. We do not trust the browser: we ask the provider
     * what really happened, then update the booking.
     */
    public AppointmentResponse check(String token) {
        AppointmentResponse appointment = appointmentService.findByPublicToken(token);
        if (appointment.status() == AppointmentStatus.PENDING_PAYMENT) {
            settle(appointment);
            return appointmentService.findByPublicToken(token);
        }
        return appointment;
    }

    /**
     * Called by the scheduled job for bookings whose hold time ran out. Last chance: if the customer actually
     * paid, keep the booking; otherwise cancel the payment and release the time.
     */
    public void expire(String token) {
        AppointmentResponse appointment = appointmentService.findByPublicToken(token);
        if (appointment.status() != AppointmentStatus.PENDING_PAYMENT) {
            return;
        }
        PaymentGatewayState state = gateway.getState(token);
        if (state == PaymentGatewayState.AUTHORIZED || state == PaymentGatewayState.CAPTURED) {
            settle(appointment);
            return;
        }
        try {
            gateway.cancel(token);
        } catch (PaymentGatewayException e) {
            log.warn("Could not cancel payment {} at the provider: {}", token, e.getMessage());
        }
        appointmentService.markPaymentFailed(token);
        log.info("Released unpaid booking {}", token);
    }

    // Sends the money back before the booking is cancelled
    public void refund(AppointmentResponse appointment) {
        gateway.refund(appointment.publicToken(), appointment.totalPriceNok());
    }

    private void settle(AppointmentResponse appointment) {
        String token = appointment.publicToken();
        switch (gateway.getState(token)) {
            case PENDING -> {
                // Still waiting for the customer; nothing to do yet
            }
            case FAILED -> appointmentService.markPaymentFailed(token);
            case AUTHORIZED -> {
                // Money is reserved: take it, then confirm the booking
                gateway.capture(token, appointment.totalPriceNok());
                appointmentService.markPaid(token);
            }
            case CAPTURED -> appointmentService.markPaid(token);
        }
    }

    // Used by controllers: a provider failure is a "bad gateway" for the customer, not our bug
    static ResponseStatusException badGateway(PaymentGatewayException e) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Betalingstjenesten svarte ikke. Prøv igjen.", e);
    }
}
