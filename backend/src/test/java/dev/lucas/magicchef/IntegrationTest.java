package dev.lucas.magicchef;

import com.jayway.jsonpath.JsonPath;
import dev.lucas.magicchef.ai.RecipeAiClient;
import dev.lucas.magicchef.mail.EmailMessage;
import dev.lucas.magicchef.mail.EmailSender;
import jakarta.servlet.http.Cookie;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the whole application against an in-memory H2 (Flyway migrations included).
 * The AI client and the e-mail sender are Mockito mocks: tests never reach OpenAI or Resend.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String COOKIE = "mc_fgp";
    protected static final String IP_HEADER = "X-Real-IP";
    private static final Pattern TOKEN = Pattern.compile("token=([A-Za-z0-9_-]+)");

    @Autowired
    protected MockMvc mvc;

    @MockitoBean
    protected RecipeAiClient aiClient;

    @MockitoBean
    protected EmailSender emailSender;

    /** Registers a user and confirms the e-mail through the real verification endpoint. */
    protected Session register() throws Exception {
        Session session = registerUnverified();
        confirmEmail(session.email());
        return session;
    }

    protected Session registerUnverified() throws Exception {
        String email = "chef-" + UUID.randomUUID() + "@mail.com";
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, randomIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Lucas", "email": "%s", "password": "segredo123"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        return Session.from(result, email);
    }

    protected void confirmEmail(String email) throws Exception {
        mvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"%s\"}".formatted(lastVerificationToken(email))))
                .andExpect(status().isOk());
    }

    /** Reads the raw token from the last confirmation e-mail "sent" to this address. */
    protected String lastVerificationToken(String email) {
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender, atLeastOnce()).send(captor.capture());
        List<EmailMessage> sent = captor.getAllValues().stream().filter(m -> m.to().equals(email)).toList();
        Matcher matcher = TOKEN.matcher(sent.getLast().text());
        if (!matcher.find()) {
            throw new IllegalStateException("No verification link in e-mail");
        }
        return matcher.group(1);
    }

    protected static String randomIp() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "10.%d.%d.%d".formatted(random.nextInt(256), random.nextInt(256), random.nextInt(1, 255));
    }

    protected record Session(String token, String fingerprint, String email) {

        public static Session from(MvcResult result, String email) throws Exception {
            String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
            return new Session(token, result.getResponse().getCookie(COOKIE).getValue(), email);
        }

        public MockHttpServletRequestBuilder sign(MockHttpServletRequestBuilder request) {
            return request.header("Authorization", "Bearer " + token).cookie(new Cookie(COOKIE, fingerprint));
        }
    }
}
