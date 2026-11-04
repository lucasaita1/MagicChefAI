package dev.lucas.MagicChefAI.Controller;

import dev.lucas.MagicChefAI.Model.FoodItem;
import dev.lucas.MagicChefAI.Service.FoodItemService;
import dev.lucas.MagicChefAI.Service.OpenAiService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;

@RestController
@AllArgsConstructor
public class RecipeController {

    private final OpenAiService openAiService;
    private final FoodItemService service;

    @GetMapping("/generate")
    public Mono<ResponseEntity<String>> generationRecipe(){
        List<FoodItem> foodItems = service.listarItens();
        return openAiService.generationRecipe(foodItems)
                .map(recipe -> ResponseEntity.ok(recipe))
                .defaultIfEmpty(ResponseEntity.noContent().build());

    }

}
