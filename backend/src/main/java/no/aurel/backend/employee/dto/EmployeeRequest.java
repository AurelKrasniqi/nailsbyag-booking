package no.aurel.backend.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String specialties
) {
}
