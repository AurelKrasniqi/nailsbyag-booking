package no.aurel.backend.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 160 characters = one SMS (with plain letters; æøå count as plain). Longer texts are split into several parts and
// cost more. Placeholders get longer when filled in ({dato} is 4 characters longer), so staying a little below is safer.
public record MessageTemplatesRequest(
        @NotBlank @Size(max = MessageTemplatesRequest.MAX_LENGTH) String confirmationText,
        @NotBlank @Size(max = MessageTemplatesRequest.MAX_LENGTH) String reminderText
) {

    public static final int MAX_LENGTH = 160;
}
