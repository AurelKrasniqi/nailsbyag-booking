package no.aurel.backend.about.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AboutRequest(
        @NotBlank @Size(max = 100) String heading,
        @NotBlank @Size(max = 3000) String body
) {
}
