package no.aurel.backend.payment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentRequest;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.appointment.dto.BookingResult;

// Orchestrates the steps of a booking that involve both the database and the payment provider.
// Each database step commits on its own before we talk to the provider.
@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppointmentService appointmentService;
    private final PaymentService paymentService;

    public BookingResult book(AppointmentRequest request) {
        // Refuse before anything is saved: the page hides the option, but the browser is never trusted
        if (request.paymentMethod() == PaymentMethod.VIPPS && !paymentService.onlinePaymentEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nettbetaling er ikke tilgjengelig. Velg å betale i salongen.");
        }

        // 1. Hold the time (or confirm it, when paying at the salon)
        AppointmentResponse appointment = appointmentService.create(request);
        if (appointment.paymentMethod() != PaymentMethod.VIPPS) {
            return new BookingResult(appointment, null);
        }

        // 2. Online payment: ask Vipps for a payment page. If that fails, release the time again straight away.
        try {
            return new BookingResult(appointment, paymentService.start(appointment));
        } catch (PaymentGatewayException e) {
            appointmentService.markPaymentFailed(appointment.publicToken());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Kunne ikke starte betalingen med Vipps. Prøv igjen, eller velg å betale i salongen.", e);
        }
    }

    public AppointmentResponse checkPayment(String token) {
        try {
            return paymentService.check(token);
        } catch (PaymentGatewayException e) {
            throw PaymentService.badGateway(e);
        }
    }

    // A booking that was paid online is refunded first; only if that works is it cancelled
    public AppointmentResponse cancel(Long id) {
        AppointmentResponse appointment = appointmentService.findById(id);
        boolean refund = appointment.paymentMethod() == PaymentMethod.VIPPS
                && appointment.paymentStatus() == PaymentStatus.PAID;
        if (refund) {
            try {
                paymentService.refund(appointment);
            } catch (PaymentGatewayException e) {
                throw PaymentService.badGateway(e);
            }
        }
        return appointmentService.cancel(id, refund);
    }
}
