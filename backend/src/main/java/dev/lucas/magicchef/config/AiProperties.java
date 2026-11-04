package dev.lucas.magicchef.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Guard rails for the generative AI feature, bound from {@code magicchef.ai.*}.
 */
@Validated
@ConfigurationProperties(prefix = "magicchef.ai")
public record AiProperties(
        @Positive int requestsPerMinute,
        @Positive int maxInputLength,
        @Positive int maxPantryItems
) {
}
