package dev.lucas.magicchef.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

/**
 * Calls OpenAI through Spring AI's {@link ChatClient} and enforces the output contract:
 * the answer must be JSON matching {@link GeneratedRecipe} and must not leak the canary.
 */
@Slf4j
@Component
public class OpenAiRecipeClient implements RecipeAiClient {

    private static final String FAILURE = "O Chef Magic se atrapalhou na cozinha. Tente novamente em instantes.";

    private final ChatClient chatClient;
    private final BeanOutputConverter<GeneratedRecipe> converter = new BeanOutputConverter<>(GeneratedRecipe.class);

    public OpenAiRecipeClient(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public GeneratedRecipe generate(RecipePrompt prompt) {
        String raw;
        try {
            raw = chatClient.prompt()
                    .system(prompt.system() + "\n\n" + converter.getFormat())
                    .user(prompt.user())
                    .call()
                    .content();
        } catch (RuntimeException e) {
            log.error("OpenAI call failed: {}", e.getMessage());
            throw new AiResponseException(FAILURE, e);
        }

        if (raw == null || raw.isBlank()) {
            throw new AiResponseException(FAILURE);
        }
        if (raw.contains(prompt.canary())) {
            log.warn("AI response contained the system prompt canary; response discarded");
            throw new AiResponseException(FAILURE);
        }
        try {
            return converter.convert(raw);
        } catch (RuntimeException e) {
            log.warn("AI response did not match the recipe schema: {}", e.getMessage());
            throw new AiResponseException(FAILURE, e);
        }
    }
}
