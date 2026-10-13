package no.aurel.backend.payment;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.dto.AppointmentResponse;

// Public endpoint for the page the customer lands on after paying. The token in the address is the proof that
// this is their booking (a random id nobody else knows).
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // POST because it can change the booking (confirm or release it), not just read it
    @PostMapping("/{token}/check-payment")
    public AppointmentResponse checkPayment(@PathVariable String token) {
        return bookingService.checkPayment(token);
    }
}
