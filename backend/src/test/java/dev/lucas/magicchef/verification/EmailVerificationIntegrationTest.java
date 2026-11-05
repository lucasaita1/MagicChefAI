package dev.lucas.magicchef.verification;

import dev.lucas.magicchef.IntegrationTest;
import dev.lucas.magicchef.ai.GeneratedRecipe;
import dev.lucas.magicchef.mail.EmailDeliveryException;
import dev.lucas.magicchef.mail.EmailMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailVerificationIntegrationTest extends IntegrationTest {

    private static final GeneratedRecipe RECIPE = new GeneratedRecipe(false, null, "Omelete", "x", "Bip!", "FACIL",
            10, 1, List.of("2 ovos"), List.of("Bata", "Frite"), List.of());

    @Test
    void signUpSendsConfirmationLinkToTheUser() throws Exception {
        Session session = registerUnverified();

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        EmailMessage email = captor.getValue();
        assertThat(email.to()).isEqualTo(session.email());
        assertThat(email.subject()).contains("Confirme");
        assertThat(email.text()).contains("https://magicchef.test/verificar-email?token=");
        assertThat(email.html()).contains("Confirmar meu e-mail");
    }

    @Test
    void unverifiedUserCannotCallTheAiUntilConfirming() throws Exception {
        Session session = registerUnverified();
        when(aiClient.generate(any())).thenReturn(RECIPE);

        mvc.perform(session.sign(get("/api/auth/me"))).andExpect(jsonPath("$.emailVerified").value(false));
        mvc.perform(session.sign(post("/api/recipes/generate")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"ovos\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("EMAIL_NOT_VERIFIED"));
        verify(aiClient, never()).generate(any());

        confirmEmail(session.email());

        mvc.perform(session.sign(get("/api/auth/me"))).andExpect(jsonPath("$.emailVerified").value(true));
        mvc.perform(session.sign(post("/api/recipes/generate")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"ovos\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void linksAreSingleUseAndUnknownTokensAreRejected() throws Exception {
        Session session = registerUnverified();
        String token = lastVerificationToken(session.email());
        String body = "{\"token\": \"%s\"}".formatted(token);

        mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));
        mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content("{\"token\": \"forjado\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON).content("{\"token\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendInvalidatesTheOldLinkAndIsRateLimited() throws Exception {
        Session session = registerUnverified();
        String oldToken = lastVerificationToken(session.email());

        mvc.perform(session.sign(post("/api/auth/resend-verification"))).andExpect(status().isAccepted());
        verify(emailSender, times(2)).send(any());
        mvc.perform(session.sign(post("/api/auth/resend-verification"))).andExpect(status().isTooManyRequests());

        mvc.perform(post("/api/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"%s\"}".formatted(oldToken)))
                .andExpect(status().isBadRequest());
        confirmEmail(session.email());

        // Already verified: resend is a no-op
        mvc.perform(session.sign(post("/api/auth/resend-verification"))).andExpect(status().isAccepted());
        verify(emailSender, times(2)).send(any());
    }

    @Test
    void accountIsCreatedEvenIfTheEmailProviderFails() throws Exception {
        doThrow(new EmailDeliveryException("Resend fora do ar", null)).when(emailSender).send(any());

        Session session = registerUnverified();

        mvc.perform(session.sign(get("/api/auth/me"))).andExpect(status().isOk());
        mvc.perform(session.sign(post("/api/auth/resend-verification")))
                .andExpect(status().isBadGateway());
    }

    @Test
    void disposableInboxesAreRejectedAtSignUp() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, randomIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bot\",\"email\":\"bot@Mailinator.com\",\"password\":\"segredo123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("temporários")));
        verify(emailSender, never()).send(any());
    }
}
