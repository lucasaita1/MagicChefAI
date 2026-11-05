package dev.lucas.magicchef.mail;

import dev.lucas.magicchef.config.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MailConfig {

    @Bean
    public EmailSender emailSender(MailProperties properties) {
        return properties.resendEnabled()
                ? new ResendEmailSender(RestClient.builder(), properties.resendApiKey(), properties.from())
                : new LoggingEmailSender();
    }
}
