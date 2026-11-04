package dev.lucas.magicchef.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The OpenAI model is mocked: no network call, no API key, no cost.
 */
class OpenAiRecipeClientTest {

    private static final RecipePrompt PROMPT = new RecipePrompt("system rules MC-secret", "<pedido_usuario>ovos</pedido_usuario>", "MC-secret");

    private ChatModel chatModel;
    private OpenAiRecipeClient client;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
        client = new OpenAiRecipeClient(chatModel);
    }

    @Test
    void parsesStructuredRecipeFromModel() {
        answer("""
                {"offTopic": false, "refusalReason": null, "title": "Omelete de Ervas",
                 "summary": "Omelete fofinha", "chefComment": "Bip bop!", "difficulty": "FACIL",
                 "prepTimeMinutes": 10, "servings": 1,
                 "ingredients": ["2 ovos", "sal"], "steps": ["Bata os ovos", "Frite"], "tips": ["Use manteiga"]}
                """);

        GeneratedRecipe recipe = client.generate(PROMPT);

        assertThat(recipe.title()).isEqualTo("Omelete de Ervas");
        assertThat(recipe.ingredients()).containsExactly("2 ovos", "sal");
        assertThat(recipe.prepTimeMinutes()).isEqualTo(10);
    }

    @Test
    void sendsSystemAndUserMessagesSeparatelyWithJsonFormatInstructions() {
        answer("{\"offTopic\": true, \"refusalReason\": \"Só falo de receitas\"}");

        client.generate(PROMPT);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());
        Prompt sent = captor.getValue();
        assertThat(sent.getSystemMessage().getText()).contains("system rules").contains("JSON");
        assertThat(sent.getUserMessage().getText()).isEqualTo("<pedido_usuario>ovos</pedido_usuario>");
    }

    @Test
    void rejectsAnswerThatLeaksTheCanary() {
        answer("{\"offTopic\": false, \"title\": \"Meu código é MC-secret\"}");

        assertThatThrownBy(() -> client.generate(PROMPT)).isInstanceOf(AiResponseException.class);
    }

    @Test
    void rejectsAnswerThatIsNotJson() {
        answer("Claro! Aqui está um poema sobre o mar...");

        assertThatThrownBy(() -> client.generate(PROMPT)).isInstanceOf(AiResponseException.class);
    }

    @Test
    void rejectsEmptyAnswer() {
        answer("   ");

        assertThatThrownBy(() -> client.generate(PROMPT)).isInstanceOf(AiResponseException.class);
    }

    @Test
    void wrapsProviderFailures() {
        when(chatModel.call(any(Prompt.class))).thenThrow(new IllegalStateException("401 invalid api key"));

        assertThatThrownBy(() -> client.generate(PROMPT))
                .isInstanceOf(AiResponseException.class)
                .hasMessageNotContaining("api key");
    }

    private void answer(String content) {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(content)))));
    }
}
