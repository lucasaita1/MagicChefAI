package dev.lucas.magicchef.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateRecipeRequest(
        @NotBlank(message = "Conte ao Chef quais ingredientes você tem")
        @Size(max = 500, message = "Mensagem muito longa (máximo de 500 caracteres)")
        String message,

        @Size(max = 30, message = "Selecione no máximo 30 itens da despensa")
        List<Long> pantryItemIds
) {
}
