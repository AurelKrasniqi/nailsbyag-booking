package no.aurel.backend.addon;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.addon.dto.AddOnResponse;
import no.aurel.backend.addon.dto.AddOnUpdateRequest;

@RestController
@RequestMapping("/api/add-ons")
@RequiredArgsConstructor
public class AddOnController {

    private final AddOnService service;

    // Public: the booking page needs the list of extras
    @GetMapping
    public List<AddOnResponse> findActive() {
        return service.findActive();
    }

    // Admin only (not in the public list of SecurityConfig): includes extras that are switched off
    @GetMapping("/all")
    public List<AddOnResponse> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public AddOnResponse update(@PathVariable Long id, @Valid @RequestBody AddOnUpdateRequest request) {
        return service.update(id, request);
    }
}
