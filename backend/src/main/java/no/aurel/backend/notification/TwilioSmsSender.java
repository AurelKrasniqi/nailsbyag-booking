package no.aurel.backend.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// The real Twilio Messages API. Active with app.sms.provider=twilio.
// Twilio takes a form-encoded POST with the account SID and auth token as HTTP Basic credentials.
@Component
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {

    private final RestClient client;
    private final String accountSid;
    private final String from;

    public TwilioSmsSender(
            @Value("${app.sms.twilio.account-sid}") String accountSid,
            @Value("${app.sms.twilio.auth-token}") String authToken,
            @Value("${app.sms.twilio.from}") String from) {
        this.client = RestClient.builder()
                .baseUrl("https://api.twilio.com")
                .defaultHeaders(h -> h.setBasicAuth(accountSid, authToken))
                .build();
        this.accountSid = accountSid;
        this.from = from;
    }

    @Override
    public void send(String toE164, String text) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", toE164);
        form.add("From", from);
        form.add("Body", text);
        try {
            client.post()
                    .uri("/2010-04-01/Accounts/{sid}/Messages.json", accountSid)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new SmsException("Twilio request failed: " + e.getMessage(), e);
        }
    }
}
