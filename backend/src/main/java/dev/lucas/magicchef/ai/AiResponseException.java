package dev.lucas.magicchef.ai;

/**
 * The AI provider failed or returned something that is not a valid, safe recipe.
 */
public class AiResponseException extends RuntimeException {

    public AiResponseException(String message) {
        super(message);
    }

    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
