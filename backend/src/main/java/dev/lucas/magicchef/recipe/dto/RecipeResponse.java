package dev.lucas.magicchef.recipe.dto;

import dev.lucas.magicchef.recipe.Recipe;

import java.time.Instant;
import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        String summary,
        String chefComment,
        String difficulty,
        Integer prepTimeMinutes,
        Integer servings,
        List<String> ingredients,
        List<String> steps,
        List<String> tips,
        String userRequest,
        boolean favorite,
        Instant createdAt
) {

    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(recipe.getId(), recipe.getTitle(), recipe.getSummary(), recipe.getChefComment(),
                recipe.getDifficulty(), recipe.getPrepTimeMinutes(), recipe.getServings(),
                List.copyOf(recipe.getIngredients()), List.copyOf(recipe.getSteps()), List.copyOf(recipe.getTips()),
                recipe.getUserRequest(), recipe.isFavorite(), recipe.getCreatedAt());
    }
}
