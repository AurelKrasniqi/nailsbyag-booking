package no.aurel.backend.addon.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

// What the admin may change on an extra: price, time and whether it is offered. The name and which treatments
// it fits are part of the price list's structure and stay fixed.
public record AddOnUpdateRequest(
        @PositiveOrZero int priceNok,
        @Positive int durationMinutes,
        boolean active
) {
}
