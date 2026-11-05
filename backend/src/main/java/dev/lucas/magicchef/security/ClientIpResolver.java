package dev.lucas.magicchef.security;

import dev.lucas.magicchef.config.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Finds the real client IP for rate limiting. Behind the nginx gateway the API is only reachable
 * through the private network, so the header the gateway sets (e.g. {@code X-Real-IP}) can be
 * trusted. When no header is configured the socket address is used, which cannot be spoofed.
 */
@Component
public class ClientIpResolver {

    private final String header;

    public ClientIpResolver(SecurityProperties properties) {
        this.header = properties.clientIpHeader();
    }

    public String resolve(HttpServletRequest request) {
        if (header != null && !header.isBlank()) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
