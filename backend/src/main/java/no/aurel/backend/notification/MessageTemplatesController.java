package no.aurel.backend.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.notification.dto.MessageTemplatesRequest;
import no.aurel.backend.notification.dto.MessageTemplatesResponse;

// Admin only: everything under /api needs the ADMIN role unless SecurityConfig lists it as public
@RestController
@RequestMapping("/api/admin/message-templates")
@RequiredArgsConstructor
public class MessageTemplatesController {

    private final MessageTemplateService service;

    @GetMapping
    public MessageTemplatesResponse get() {
        return service.get();
    }

    @PutMapping
    public MessageTemplatesResponse update(@Valid @RequestBody MessageTemplatesRequest request) {
        return service.update(request);
    }
}
