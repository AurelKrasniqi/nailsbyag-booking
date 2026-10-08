package no.aurel.backend.salonservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SalonServiceRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Positive int durationMinutes,
        @PositiveOrZero int priceNok,
        boolean active
) {
}
