package no.aurel.backend.appointment.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import no.aurel.backend.customer.dto.CustomerRequest;

// Public booking request: the customer has no account, so their details come with the booking
public record AppointmentRequest(
        @NotNull Long employeeId,
        @NotEmpty List<@NotNull Long> serviceIds,
        @NotNull LocalDateTime startTime,
        @NotNull @Valid CustomerRequest customer
) {
}
