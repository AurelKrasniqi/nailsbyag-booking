package no.aurel.backend.notification;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Sends through the Resend HTTP API. Active with app.email.provider=resend.
// HTTP (not SMTP) on purpose: hosting platforms often block outgoing SMTP ports, but HTTPS always works.
// The "from" address must be on a domain verified in Resend (here thebeautyroombyag.no).
@Component
@ConditionalOnProperty(name = "app.email.provider", havingValue = "resend")
public class ResendEmailSender implements EmailSender {

    private final RestClient client;
    private final String from;

    public ResendEmailSender(
            @Value("${app.email.resend.api-key}") String apiKey,
            @Value("${app.email.from}") String from) {
        this.client = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeaders(h -> h.setBearerAuth(apiKey))
                .build();
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String text) {
        Map<String, Object> body = Map.of(
                "from", from,
                "to", List.of(to),
                "subject", subject,
                "text", text);
        try {
            client.post()
                    .uri("/emails")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new EmailException("Resend request failed: " + e.getMessage(), e);
        }
    }
}
