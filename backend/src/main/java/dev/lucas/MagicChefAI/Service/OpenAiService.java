package dev.lucas.MagicChefAI.Service;

import dev.lucas.MagicChefAI.Model.FoodItem;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class OpenAiService {

    private final WebClient webClient;
    private final String apiKey;

    @Autowired
    public OpenAiService(WebClient.Builder webClientBuilder) {
        Dotenv dotenv = Dotenv.load(); // <-- carrega o .env
        this.apiKey = dotenv.get("API_KEY"); // <-- lê a chave
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com").build();
    }


    public Mono<String> generationRecipe(List<FoodItem> foodItems) {

        String alimentos = foodItems.stream()
                .map(item -> String.format("gs (%s) - Quantidade: %d, Categoria: %s",
                        item.getNome(), item.getQuantidade(), item.getCategoria()))
                .collect(Collectors.joining("\n"));
        //monta o promp
        String prompt = "Baseado nos itens cadastrados no banco de dados, faça uma receita gostosa. (Pode usar mais items e dicas para compor a receita)\n " + alimentos ;
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Baseado nos itens cadastrados no banco de dados, faça uma receita gostosa. (Pode usar mais items e dicas para compor a receita)\n" + alimentos
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        //requisiçao do promp
        return webClient.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
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
 /*curl https://api.openai.com/v1/responses \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $OPENAI_API_KEY" \
    -d '{
        "model": "gpt-4.1",
        "input": "Write a one-sentence bedtime story about a unicorn."
    }'
    /*
     */