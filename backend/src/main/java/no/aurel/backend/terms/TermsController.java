package no.aurel.backend.terms;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.terms.dto.TermsSectionRequest;
import no.aurel.backend.terms.dto.TermsSectionResponse;

// GET is public (the terms page); changing it is admin only (see SecurityConfig)
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Validated
public class TermsController {

    private final TermsService service;

    @GetMapping
    public List<TermsSectionResponse> get() {
        return service.findAll();
    }

    @PutMapping
    public List<TermsSectionResponse> replace(
            @RequestBody @NotEmpty @Size(max = 30) List<@Valid TermsSectionRequest> sections) {
        return service.replaceAll(sections);
    }
}
