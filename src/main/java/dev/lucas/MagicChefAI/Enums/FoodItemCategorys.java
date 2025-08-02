package dev.lucas.MagicChefAI.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum FoodItemCategorys {
        GRAO,
        CARNE,
        LEGUME,
        LATICINIO,
        PADARIA,
        PROTEINA,
        VERDURA,
        FRUTA,
        MASSAS,
        DOCES,
        BEBIDAS;

        @JsonCreator
        public static FoodItemCategorys fromString(String key) {
                if (key == null) return null;
                return FoodItemCategorys.valueOf(key.toUpperCase());
        }
    }