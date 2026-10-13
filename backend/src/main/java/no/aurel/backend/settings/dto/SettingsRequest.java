package no.aurel.backend.settings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SettingsRequest(
        boolean vatRegistered,
        @Min(0) @Max(100) int vatRatePercent
) {
}
