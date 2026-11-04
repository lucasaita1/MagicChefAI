package dev.lucas.magicchef;

import com.jayway.jsonpath.JsonPath;
import dev.lucas.magicchef.ai.RecipeAiClient;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the whole application against an in-memory H2 (Flyway migrations included).
 * The AI client is a Mockito mock, so tests never reach OpenAI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    protected static final String COOKIE = "mc_fgp";

    @Autowired
    protected MockMvc mvc;

    @MockitoBean
    protected RecipeAiClient aiClient;

    protected Session register() throws Exception {
        String email = "chef-" + UUID.randomUUID() + "@mail.com";
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Lucas", "email": "%s", "password": "segredo123"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();
        return Session.from(result, email);
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
