package dev.lucas.magicchef.recipe;

import dev.lucas.magicchef.recipe.dto.GenerateRecipeRequest;
import dev.lucas.magicchef.recipe.dto.RecipeResponse;
import dev.lucas.magicchef.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Receitas")
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @Operation(summary = "Pergunta ao Chef IA: gera uma receita a partir dos ingredientes")
    @PostMapping("/generate")
    public ResponseEntity<RecipeResponse> generate(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @Valid @RequestBody GenerateRecipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.generate(user.id(), request));
    }

    @Operation(summary = "Minhas receitas (mais recentes primeiro)")
    @GetMapping
    public List<RecipeResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return recipeService.list(user.id());
    }

    @Operation(summary = "Detalhe de uma receita")
    @GetMapping("/{id}")
    public RecipeResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return recipeService.get(user.id(), id);
    }

    @Operation(summary = "Marca ou desmarca como favorita")
    @PatchMapping("/{id}/favorite")
    public RecipeResponse toggleFavorite(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return recipeService.toggleFavorite(user.id(), id);
    }

    @Operation(summary = "Exclui uma receita")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        recipeService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
