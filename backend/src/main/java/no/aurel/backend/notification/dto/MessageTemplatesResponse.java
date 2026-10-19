package no.aurel.backend.notification.dto;

import java.util.List;

public record MessageTemplatesResponse(
        String confirmationText,
        String reminderText,
        String defaultConfirmationText,
        String defaultReminderText,
        List<String> placeholders
) {
}
