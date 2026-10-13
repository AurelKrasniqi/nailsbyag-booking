package no.aurel.backend.settings;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.settings.dto.SettingsRequest;
import no.aurel.backend.settings.dto.SettingsResponse;

// Admin only (everything under /api/admin needs the ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService service;

    @GetMapping
    public SettingsResponse get() {
        return service.get();
    }

    @PutMapping
    public SettingsResponse update(@Valid @RequestBody SettingsRequest request) {
        return service.update(request);
    }
}
