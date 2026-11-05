package dev.lucas.magicchef.auth;

import dev.lucas.magicchef.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends IntegrationTest {

    @Test
    void registerIssuesTokenAndHardenedFingerprintCookie() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, randomIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ANA@Mail.com\",\"password\":\"segredo123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.user.emailVerified").value(false))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/api")))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).doesNotContain("password");
    }

    @Test
    void loginAndMeWorkWithTokenAndCookie() throws Exception {
        Session registered = register();

        MvcResult login = mvc.perform(login(registered.email(), "segredo123").header(IP_HEADER, randomIp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andReturn();
        Session session = Session.from(login, registered.email());

        mvc.perform(session.sign(get("/api/auth/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(registered.email()))
                .andExpect(jsonPath("$.name").value("Lucas"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void wrongPasswordReturnsGenericError() throws Exception {
        Session session = register();

        mvc.perform(login(session.email(), "errada123").header(IP_HEADER, randomIp()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));

        mvc.perform(login("nobody@mail.com", "errada123").header(IP_HEADER, randomIp()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos."));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        Session session = register();

        mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, randomIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outro\",\"email\":\"%s\",\"password\":\"segredo123\"}".formatted(session.email())))
                .andExpect(status().isConflict());
    }

    @Test
    void weakInputIsRejectedWithFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, randomIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"invalido\",\"password\":\"curta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void logoutRevokesTheTokenImmediately() throws Exception {
        Session session = register();

        mvc.perform(session.sign(post("/api/auth/logout")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        mvc.perform(session.sign(get("/api/auth/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stolenTokenWithoutFingerprintCookieIsRejectedAndSessionBurned() throws Exception {
        Session session = register();

        // Attacker copied the JWT (e.g. via XSS) but cannot read the HttpOnly cookie
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + session.token())
                        .cookie(new Cookie(COOKIE, "forged-fingerprint")))
                .andExpect(status().isUnauthorized());

        // The session was revoked, so even the legitimate pair stops working
        mvc.perform(session.sign(get("/api/auth/me")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedOrMissingTokenIsRejected() throws Exception {
        Session session = register();
        String tampered = session.token().substring(0, session.token().length() - 3) + "abc";

        mvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + tampered)
                        .cookie(new Cookie(COOKIE, session.fingerprint())))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void bruteForceOnOneAccountIsRateLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            mvc.perform(login("target@mail.com", "chute1234").header(IP_HEADER, "203.0.113.7"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(login("target@mail.com", "chute1234").header(IP_HEADER, "203.0.113.7"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void oneIpTryingManyAccountsIsRateLimited() throws Exception {
        for (int i = 0; i < 15; i++) {
            mvc.perform(login("victim" + i + "@mail.com", "chute1234").header(IP_HEADER, "203.0.113.50"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(login("victim99@mail.com", "chute1234").header(IP_HEADER, "203.0.113.50"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value(containsString("desta rede")));

        // Another IP is not affected
        mvc.perform(login("victim99@mail.com", "chute1234").header(IP_HEADER, "203.0.113.51"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void oneIpCreatingManyAccountsIsRateLimited() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/auth/register")
                            .header(IP_HEADER, "198.51.100.9")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Bot\",\"email\":\"bot%d-%d@mail.com\",\"password\":\"segredo123\"}"
                                    .formatted(i, System.nanoTime())))
                    .andExpect(status().isCreated());
        }
        mvc.perform(post("/api/auth/register")
                        .header(IP_HEADER, "198.51.100.9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bot\",\"email\":\"bot-last@mail.com\",\"password\":\"segredo123\"}"))
                .andExpect(status().isTooManyRequests());
    }

    private static MockHttpServletRequestBuilder login(String email, String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password));
    }
}
