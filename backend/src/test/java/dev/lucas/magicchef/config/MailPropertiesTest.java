package dev.lucas.magicchef.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MailPropertiesTest {

    @Test
    void stripsQuotesKeptByDotEnvFiles() {
        MailProperties properties = new MailProperties("'re_key'", " \"MagicChefAI <contato@tribebr.com>\" ",
                "http://localhost:5173", Duration.ofHours(24));

        assertThat(properties.from()).isEqualTo("MagicChefAI <contato@tribebr.com>");
        assertThat(properties.resendApiKey()).isEqualTo("re_key");
        assertThat(properties.resendEnabled()).isTrue();
    }

    @Test
    void keepsUnquotedValuesAndToleratesMissingKey() {
        MailProperties properties = new MailProperties(null, "chef@magicchef.dev", "http://x", Duration.ofHours(1));

        assertThat(properties.from()).isEqualTo("chef@magicchef.dev");
        assertThat(properties.resendEnabled()).isFalse();
    }
}
