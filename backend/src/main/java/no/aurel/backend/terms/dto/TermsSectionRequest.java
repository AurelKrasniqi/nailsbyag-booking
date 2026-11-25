package no.aurel.backend.terms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// heading may be blank (no heading); the text may not
public record TermsSectionRequest(
        @Size(max = 100) String heading,
        @NotBlank @Size(max = 5000) String body
) {
}
