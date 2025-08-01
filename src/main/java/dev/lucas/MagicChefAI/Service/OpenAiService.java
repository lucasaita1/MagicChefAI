package dev.lucas.MagicChefAI.Service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class OpenAiService {

    private final WebClient webClient;
    private String apiKey = System.getenv("API_KEY");

    public Mono<String> generationRecipe() {
        String prompt = "Me sugira uma receita com ingredientes comuns nas geladeiras do Brasil.";
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Me sugira uma receita com ingredientes comuns nas geladeiras do Brasil."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );
        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    // Extrai a lista de choices
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");

                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        if (message != null && message.get("content") != null) {
                            return message.get("content").toString();
                        }
                    }

                    return "Nenhuma receita foi gerada.";
                });
    }
}
