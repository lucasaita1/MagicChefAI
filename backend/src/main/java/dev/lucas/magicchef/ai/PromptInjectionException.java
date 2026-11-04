package dev.lucas.magicchef.ai;

import dev.lucas.magicchef.common.BadRequestException;

public class PromptInjectionException extends BadRequestException {

    public PromptInjectionException() {
        super("O Chef Magic só fala de culinária! Envie apenas ingredientes e preferências para a sua receita.");
    }
}
