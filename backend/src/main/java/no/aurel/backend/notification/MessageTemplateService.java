package no.aurel.backend.notification;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.notification.dto.MessageTemplatesRequest;
import no.aurel.backend.notification.dto.MessageTemplatesResponse;

// Loads the editable customer texts and fills them in for an appointment.
@Service
@RequiredArgsConstructor
@Transactional
public class MessageTemplateService {

    // Same wording as the row in migration V18. Used by "Tilbakestill til standard" in the admin page.
    public static final String DEFAULT_CONFIRMATION =
            "Hei! Takk for at du booket time hos oss på thebeautyroombyag. "
                    + "Dette er en bekreftelse på bookingen din {dato} kl. {tid}. Vi ses!";
    public static final String DEFAULT_REMINDER =
            "Påminnelse: du har time hos thebeautyroombyag i morgen kl. {tid} ({dato}). Vi gleder oss til å se deg!";

    private final MessageTemplatesRepository repository;

    @Transactional(readOnly = true)
    public MessageTemplatesResponse get() {
        return toResponse(load());
    }

    public MessageTemplatesResponse update(MessageTemplatesRequest request) {
        rejectUnknownPlaceholders(request.confirmationText());
        rejectUnknownPlaceholders(request.reminderText());
        MessageTemplates templates = load();
        templates.setConfirmationText(request.confirmationText().strip());
        templates.setReminderText(request.reminderText().strip());
        return toResponse(templates);
    }

    @Transactional(readOnly = true)
    public String confirmationFor(AppointmentResponse appointment) {
        return MessageTemplateRenderer.render(load().getConfirmationText(), appointment);
    }

    @Transactional(readOnly = true)
    public String reminderFor(AppointmentResponse appointment) {
        return MessageTemplateRenderer.render(load().getReminderText(), appointment);
    }

    private void rejectUnknownPlaceholders(String text) {
        List<String> unknown = MessageTemplateRenderer.unknownPlaceholders(text);
        if (!unknown.isEmpty()) {
            String allowed = MessageTemplateRenderer.PLACEHOLDERS.stream().map(p -> "{" + p + "}").toList().toString();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ukjent plassholder {" + unknown.get(0) + "}. Gyldige: " + allowed);
        }
    }

    private static MessageTemplatesResponse toResponse(MessageTemplates t) {
        return new MessageTemplatesResponse(t.getConfirmationText(), t.getReminderText(),
                DEFAULT_CONFIRMATION, DEFAULT_REMINDER, MessageTemplateRenderer.PLACEHOLDERS);
    }

    // The row is created by the migration, so it must exist
    private MessageTemplates load() {
        return repository.findById(MessageTemplates.ID)
                .orElseThrow(() -> new IllegalStateException("message_templates row is missing"));
    }
}
