package no.aurel.backend.appointment;

import no.aurel.backend.appointment.dto.AppointmentResponse;

// Published when a booking becomes final: right away for "pay at the salon", or when Vipps confirms the payment.
// Other parts of the app (text messages, later maybe e-mail) listen for it, so AppointmentService does not
// need to know about them.
public record BookingConfirmedEvent(AppointmentResponse appointment) {
}
