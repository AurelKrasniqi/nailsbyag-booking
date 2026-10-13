package no.aurel.backend.addon;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.addon.dto.AddOnResponse;

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
}
