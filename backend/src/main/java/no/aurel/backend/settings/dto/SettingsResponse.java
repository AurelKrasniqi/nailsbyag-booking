package no.aurel.backend.settings.dto;

import no.aurel.backend.settings.BusinessSettings;

public record SettingsResponse(boolean vatRegistered, int vatRatePercent) {

    public static SettingsResponse from(BusinessSettings entity) {
        return new SettingsResponse(entity.isVatRegistered(), entity.getVatRatePercent());
    }
}
