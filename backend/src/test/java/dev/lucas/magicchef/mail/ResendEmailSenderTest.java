package dev.lucas.magicchef.mail;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Resend is simulated with MockRestServiceServer: no network, no real key.
 */
class ResendEmailSenderTest {

    private static final EmailMessage MESSAGE = new EmailMessage("lucas@mail.com", "Assunto", "<b>oi</b>", "oi");

    @Test
    void postsTheMessageToResendWithBearerKey() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_test"))
                .andExpect(jsonPath("$.from").value("Chef <chef@magicchef.dev>"))
                .andExpect(jsonPath("$.to[0]").value("lucas@mail.com"))
                .andExpect(jsonPath("$.subject").value("Assunto"))
                .andExpect(jsonPath("$.html").value("<b>oi</b>"))
                .andRespond(withSuccess("{\"id\":\"123\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        new ResendEmailSender(builder, "re_test", "Chef <chef@magicchef.dev>").send(MESSAGE);

        server.verify();
    }

    @Test
    void wrapsProviderErrors() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.resend.com/emails")).andRespond(withStatus(HttpStatus.FORBIDDEN));

        ResendEmailSender sender = new ResendEmailSender(builder, "re_test", "chef@magicchef.dev");

        assertThatThrownBy(() -> sender.send(MESSAGE))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessageContaining("Não foi possível enviar");
    }

    @Test
    void loggingSenderNeverFails() {
        new LoggingEmailSender().send(MESSAGE);
    }
}
