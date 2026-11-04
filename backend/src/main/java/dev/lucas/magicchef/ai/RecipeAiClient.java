package dev.lucas.magicchef.ai;

/**
 * Port to the generative AI provider. Tests replace it with a mock, so no real call is made.
 */
public interface RecipeAiClient {

    GeneratedRecipe generate(RecipePrompt prompt);
}
