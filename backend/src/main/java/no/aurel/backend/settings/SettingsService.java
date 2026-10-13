package no.aurel.backend.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.settings.dto.SettingsRequest;
import no.aurel.backend.settings.dto.SettingsResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingsService {

    private final BusinessSettingsRepository repository;

    @Transactional(readOnly = true)
    public SettingsResponse get() {
        return SettingsResponse.from(load());
    }

    public SettingsResponse update(SettingsRequest request) {
        BusinessSettings settings = load();
        settings.setVatRegistered(request.vatRegistered());
        settings.setVatRatePercent(request.vatRatePercent());
        return SettingsResponse.from(settings);
    }

    // The row is created by the migration, so it must exist
    private BusinessSettings load() {
        return repository.findById(BusinessSettings.ID)
                .orElseThrow(() -> new IllegalStateException("business_settings row is missing"));
    }
}
