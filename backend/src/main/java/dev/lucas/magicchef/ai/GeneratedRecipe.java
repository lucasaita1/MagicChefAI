package dev.lucas.magicchef.ai;

import java.util.List;

/**
 * Structured output the model must return (JSON schema enforced by Spring AI's
 * {@code BeanOutputConverter}). {@code offTopic} lets the model refuse non-culinary requests.
 */
public record GeneratedRecipe(
        boolean offTopic,
        String refusalReason,
        String title,
        String summary,
        String chefComment,
        String difficulty,
        Integer prepTimeMinutes,
        Integer servings,
        List<String> ingredients,
        List<String> steps,
        List<String> tips
) {
}
