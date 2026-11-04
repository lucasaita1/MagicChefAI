package dev.lucas.magicchef.pantry.dto;

import dev.lucas.magicchef.pantry.IngredientCategory;
import dev.lucas.magicchef.pantry.MeasureUnit;
import dev.lucas.magicchef.pantry.PantryItem;
import dev.lucas.magicchef.pantry.StorageLocation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PantryItemResponse(
        Long id,
        String name,
        IngredientCategory category,
        StorageLocation location,
        BigDecimal quantity,
        MeasureUnit unit,
        LocalDate expiresAt
) {

    public static PantryItemResponse from(PantryItem item) {
        return new PantryItemResponse(item.getId(), item.getName(), item.getCategory(), item.getLocation(),
                item.getQuantity(), item.getUnit(), item.getExpiresAt());
    }
}
