package no.aurel.backend.payment;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.JsonNode;

// The real Vipps MobilePay ePayment API. Active with app.payment.provider=vipps.
// All amounts toward Vipps are in øre (minor units): 750 kr is sent as 75000.
// Not yet run against Vipps: needs the merchant's test credentials (see the VIPPS_* settings).
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "vipps")
public class VippsPaymentGateway implements PaymentGateway {

    private final RestClient client;
    private final String clientId;
    private final String clientSecret;
    private final String subscriptionKey;
    private final String merchantSerialNumber;
    private final String frontendBaseUrl;

    private String accessToken;
    private Instant accessTokenExpiresAt = Instant.EPOCH;

    public VippsPaymentGateway(
            @Value("${app.payment.vipps.base-url}") String baseUrl,
            @Value("${app.payment.vipps.client-id}") String clientId,
            @Value("${app.payment.vipps.client-secret}") String clientSecret,
            @Value("${app.payment.vipps.subscription-key}") String subscriptionKey,
            @Value("${app.payment.vipps.merchant-serial-number}") String merchantSerialNumber,
            @Value("${app.payment.frontend-base-url}") String frontendBaseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.subscriptionKey = subscriptionKey;
        this.merchantSerialNumber = merchantSerialNumber;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public String createPayment(String reference, int amountNok, String description) {
        Map<String, Object> body = Map.of(
                "amount", amount(amountNok),
                "paymentMethod", Map.of("type", "WALLET"),
                "reference", reference,
                "returnUrl", frontendBaseUrl + "/#/betaling/" + reference,
                "userFlow", "WEB_REDIRECT",
                "paymentDescription", description);
        JsonNode response = call(() -> client.post()
                .uri("/epayment/v1/payments")
                .headers(h -> apiHeaders(h, reference))
                .body(body)
                .retrieve()
                .body(JsonNode.class));
        return response.path("redirectUrl").asString();
    }

    @Override
    public PaymentGatewayState getState(String reference) {
        JsonNode payment = call(() -> client.get()
                .uri("/epayment/v1/payments/{reference}", reference)
                .headers(h -> apiHeaders(h, null))
                .retrieve()
                .body(JsonNode.class));

        long authorized = payment.path("aggregate").path("authorizedAmount").path("value").asLong(0);
        long captured = payment.path("aggregate").path("capturedAmount").path("value").asLong(0);

        return switch (payment.path("state").asString()) {
            case "CREATED" -> PaymentGatewayState.PENDING;
            case "AUTHORIZED" -> captured > 0 && captured >= authorized
                    ? PaymentGatewayState.CAPTURED
                    : PaymentGatewayState.AUTHORIZED;
            // ABORTED, EXPIRED, TERMINATED
            default -> PaymentGatewayState.FAILED;
        };
    }

    @Override
    public void capture(String reference, int amountNok) {
        modify(reference, "capture", amountNok);
    }

    @Override
    public void cancel(String reference) {
        call(() -> client.post()
                .uri("/epayment/v1/payments/{reference}/cancel", reference)
                .headers(h -> apiHeaders(h, reference + "-cancel"))
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void refund(String reference, int amountNok) {
        modify(reference, "refund", amountNok);
    }

    // capture and refund have the same shape. The idempotency key makes a retry safe: Vipps performs it only once.
    private void modify(String reference, String action, int amountNok) {
        Map<String, Object> body = Map.of("modificationAmount", amount(amountNok));
        call(() -> client.post()
                .uri("/epayment/v1/payments/{reference}/" + action, reference)
                .headers(h -> apiHeaders(h, reference + "-" + action))
                .body(body)
                .retrieve()
                .toBodilessEntity());
    }

    private static Map<String, Object> amount(int amountNok) {
        return Map.of("currency", "NOK", "value", amountNok * 100L);
    }

    private void apiHeaders(HttpHeaders headers, String idempotencyKey) {
        headers.setBearerAuth(accessToken());
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.set("Merchant-Serial-Number", merchantSerialNumber);
        headers.set("Vipps-System-Name", "nailsbyag");
        headers.set("Vipps-System-Version", "1.0");
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null) {
            headers.set("Idempotency-Key", idempotencyKey);
        }
    }

    // The token lasts a while (about an hour), so it is reused until shortly before it expires
    private synchronized String accessToken() {
        if (accessToken == null || Instant.now().isAfter(accessTokenExpiresAt.minusSeconds(60))) {
            JsonNode response = call(() -> client.post()
                    .uri("/accesstoken/get")
                    .header("client_id", clientId)
                    .header("client_secret", clientSecret)
                    .header("Ocp-Apim-Subscription-Key", subscriptionKey)
                    .header("Merchant-Serial-Number", merchantSerialNumber)
                    .retrieve()
                    .body(JsonNode.class));
            accessToken = response.path("access_token").asString();
            long seconds = response.path("expires_in").asLong(0);
            accessTokenExpiresAt = Instant.now().plus(Duration.ofSeconds(seconds > 120 ? seconds : 300));
        }
        return accessToken;
    }

    // Any network or HTTP error from Vipps becomes one exception type that the rest of the app understands
    private <T> T call(java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            throw new PaymentGatewayException("Vipps request failed: " + e.getMessage(), e);
        }
    }
}
