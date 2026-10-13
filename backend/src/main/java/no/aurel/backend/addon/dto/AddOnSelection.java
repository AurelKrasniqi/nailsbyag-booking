package no.aurel.backend.addon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// "This many of this extra": e.g. 4 nails with glitter. Used both when asking for free times and when booking.
public record AddOnSelection(@NotNull Long addOnId, @Min(1) int quantity) {
}
