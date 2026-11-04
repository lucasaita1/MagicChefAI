package dev.lucas.magicchef.pantry.dto;

import dev.lucas.magicchef.pantry.IngredientCategory;
import dev.lucas.magicchef.pantry.MeasureUnit;
import dev.lucas.magicchef.pantry.StorageLocation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PantryItemRequest(
        @NotBlank(message = "Informe o nome do ingrediente")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        // Letters (with accents), digits, spaces and light punctuation only: these names are sent to the AI
        @Pattern(regexp = "^[\\p{L}\\p{N} ,.'()%-]+$", message = "O nome contém caracteres não permitidos")
        String name,

        @NotNull(message = "Informe a categoria")
        IngredientCategory category,

        @NotNull(message = "Informe se fica na geladeira ou no armário")
        StorageLocation location,

        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
        @DecimalMax(value = "99999", message = "Quantidade muito alta")
        BigDecimal quantity,

        @NotNull(message = "Informe a unidade")
        MeasureUnit unit,

        LocalDate expiresAt
) {
}
