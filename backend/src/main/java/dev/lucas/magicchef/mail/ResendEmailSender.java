package dev.lucas.magicchef.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Sends e-mails through the Resend HTTP API (https://resend.com/docs/api-reference/emails/send-email).
 */
@Slf4j
public class ResendEmailSender implements EmailSender {

    static final String BASE_URL = "https://api.resend.com";

    private final RestClient restClient;
    private final String from;

    public ResendEmailSender(RestClient.Builder builder, String apiKey, String from) {
        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.from = from;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", from,
                            "to", List.of(message.to()),
                            "subject", message.subject(),
                            "html", message.html(),
                            "text", message.text()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Resend rejected the e-mail: {}", e.getMessage());
            throw new EmailDeliveryException("Não foi possível enviar o e-mail agora. Tente novamente em instantes.", e);
        }
    }
}
