package no.aurel.backend.appointment.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @NotNull Long customerId,
        @NotNull Long employeeId,
        @NotNull Long serviceId,
        @NotNull LocalDateTime startTime
) {
}
