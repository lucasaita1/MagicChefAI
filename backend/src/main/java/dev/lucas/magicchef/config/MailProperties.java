package dev.lucas.magicchef.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * E-mail delivery settings bound from {@code magicchef.mail.*}. Without a Resend API key the
 * app logs the verification link instead of sending it (local development only).
 */
@Validated
@ConfigurationProperties(prefix = "magicchef.mail")
public record MailProperties(
        String resendApiKey,
        @NotBlank String from,
        @NotBlank String appBaseUrl,
        @NotNull Duration verificationTtl
) {

    public boolean resendEnabled() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }
}
