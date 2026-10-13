package no.aurel.backend.appointment.dto;

// What a new booking returns. paymentRedirectUrl is only set when the customer chose to pay online:
// the browser must then be sent there to complete the payment.
public record BookingResult(AppointmentResponse appointment, String paymentRedirectUrl) {
}
