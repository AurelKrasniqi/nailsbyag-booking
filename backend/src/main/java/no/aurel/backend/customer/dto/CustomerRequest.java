package no.aurel.backend.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import no.aurel.backend.customer.validation.ValidPhone;

public record CustomerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 30) @ValidPhone String phone,
        @Email @Size(max = 150) String email
) {
}
