package no.aurel.backend.servicegroup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceGroupRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 100) String eyebrow,
        // 0 = the top of a tall photo stays visible, 100 = the bottom
        @Min(0) @Max(100) int imageFocus
) {
}
