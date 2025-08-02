package dev.lucas.MagicChefAI.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${chatgpt.api.url:https://api.openai.com/v1/chat/completions}")
    private String chatGptURL;

    @Bean
    public WebClient webClient (WebClient.Builder builder){
        return builder.baseUrl(chatGptURL).build();
    }
}
