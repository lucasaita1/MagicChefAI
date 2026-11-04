package dev.lucas.magicchef.ai;

/**
 * A ready-to-send prompt. {@code canary} is a random secret embedded in the system prompt;
 * if it ever shows up in the answer, the model leaked its instructions.
 */
public record RecipePrompt(String system, String user, String canary) {
}
