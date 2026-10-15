package no.aurel.backend.addon.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Everything the admin can set on an extra, used both to create one and to change one.
//   unitLabel:   what the price is per, e.g. "pr. negl". Null = a one-off extra.
//   maxQuantity: 1 = yes or no, more = chosen per unit (e.g. up to 10 nails)
//   groupIds:    the boxes it can be added to
public record AddOnUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 30) String unitLabel,
        @PositiveOrZero int priceNok,
        @Positive int durationMinutes,
        @Positive int maxQuantity,
        @NotEmpty Set<Long> groupIds,
        boolean active
) {
}
