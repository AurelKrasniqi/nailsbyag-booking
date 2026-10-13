package no.aurel.backend.appointment;

public enum AppointmentStatus {
    // Online booking waiting for the Vipps payment. The time is held but not confirmed.
    PENDING_PAYMENT,
    BOOKED,
    CANCELLED,
    COMPLETED
}
