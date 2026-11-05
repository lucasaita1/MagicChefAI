package dev.lucas.magicchef.security;

import dev.lucas.magicchef.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void usesTrustedGatewayHeaderWhenConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Real-IP", " 177.10.20.30 , 10.0.0.9");

        assertThat(resolver("X-Real-IP").resolve(request)).isEqualTo("177.10.20.30");
    }

    @Test
    void fallsBackToSocketAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Real-IP", "1.2.3.4");

        assertThat(resolver("X-Real-IP").resolve(new MockHttpServletRequest() {{ setRemoteAddr("10.0.0.3"); }}))
                .isEqualTo("10.0.0.3");
        // Without a configured header, client-supplied headers are ignored (cannot be spoofed)
        assertThat(resolver("").resolve(request)).isEqualTo("10.0.0.2");
        assertThat(resolver(null).resolve(request)).isEqualTo("10.0.0.2");
    }

    private static ClientIpResolver resolver(String header) {
        return new ClientIpResolver(new SecurityProperties(null, null, null, 10, 20, 5, header));
    }
}
