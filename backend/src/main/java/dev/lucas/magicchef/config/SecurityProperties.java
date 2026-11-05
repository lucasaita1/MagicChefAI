package dev.lucas.magicchef.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Security settings bound from {@code magicchef.security.*}.
 * The application refuses to start without a strong JWT secret.
 */
@Validated
@ConfigurationProperties(prefix = "magicchef.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cookie cookie,
        @Valid @NotNull Cors cors,
        @Positive int loginAttemptsPerMinute,
        @Positive int loginIpAttemptsPerMinute,
        @Positive int registerIpAttemptsPerMinute,
        String clientIpHeader
) {

    public record Jwt(
            @NotBlank(message = "JWT_SECRET must be set")
            @Size(min = 32, message = "JWT_SECRET must have at least 32 characters")
            String secret,
            @NotBlank String issuer,
            @NotNull Duration ttl
    ) {
    }

    /**
     * Hardened cookie that carries the raw token fingerprint (OWASP token sidejacking prevention).
     */
    public record Cookie(@NotBlank String name, boolean secure, @NotBlank String sameSite) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
