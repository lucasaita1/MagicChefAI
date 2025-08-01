package dev.lucas.MagicChefAI.Controller;

import dev.lucas.MagicChefAI.Service.OpenAiService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class RecipeController {

    private final OpenAiService openAiService;

    @GetMapping
    public Mono<ResponseEntity<String>> generationRecipe(){
        return openAiService.generationRecipe();
    }

}
