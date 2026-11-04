package dev.lucas.magicchef.ai;

/**
 * The model itself classified the request as not being about cooking.
 */
public class RecipeRefusedException extends RuntimeException {

    public RecipeRefusedException(String reason) {
        super(reason);
    }
}
