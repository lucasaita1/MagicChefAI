package dev.lucas.magicchef.recipe;

import dev.lucas.magicchef.ai.AiResponseException;
import dev.lucas.magicchef.ai.GeneratedRecipe;
import dev.lucas.magicchef.ai.PromptGuard;
import dev.lucas.magicchef.ai.RecipeAiClient;
import dev.lucas.magicchef.ai.RecipePrompt;
import dev.lucas.magicchef.ai.RecipePromptFactory;
import dev.lucas.magicchef.ai.RecipeRefusedException;
import dev.lucas.magicchef.common.BadRequestException;
import dev.lucas.magicchef.common.NotFoundException;
import dev.lucas.magicchef.common.RateLimiter;
import dev.lucas.magicchef.config.AiProperties;
import dev.lucas.magicchef.pantry.PantryItem;
import dev.lucas.magicchef.pantry.PantryService;
import dev.lucas.magicchef.recipe.dto.GenerateRecipeRequest;
import dev.lucas.magicchef.recipe.dto.RecipeResponse;
import dev.lucas.magicchef.security.AuthenticatedUser;
import dev.lucas.magicchef.user.UserRepository;
import dev.lucas.magicchef.verification.EmailNotVerifiedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private static final Set<String> DIFFICULTIES = Set.of("FACIL", "MEDIO", "DIFICIL");
    private static final String INVALID_ANSWER = "O Chef Magic não conseguiu montar essa receita. Tente descrever os ingredientes de outro jeito.";
    private static final String DEFAULT_REFUSAL = "Eu só sei falar de receitas! Me conte quais ingredientes você tem.";

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final PantryService pantryService;
    private final PromptGuard promptGuard;
    private final RecipePromptFactory promptFactory;
    private final RecipeAiClient aiClient;
    private final RateLimiter rateLimiter;
    private final AiProperties aiProperties;

    /**
     * Full generation pipeline: verified e-mail, rate limit, sanitize, detect injection, build a delimited
     * prompt, call the AI and validate its structured answer before persisting it.
     */
    @Transactional
    public RecipeResponse generate(AuthenticatedUser user, GenerateRecipeRequest request) {
        if (!user.emailVerified()) {
            throw new EmailNotVerifiedException();
        }
        Long userId = user.id();
        rateLimiter.acquire("ai:" + userId, aiProperties.requestsPerMinute(),
                "O Chef Magic está com muitas panelas no fogo! Aguarde um minuto para pedir outra receita.");

        String message = promptGuard.sanitize(request.message());
        if (message.isBlank()) {
            throw new BadRequestException("Conte ao Chef quais ingredientes você tem.");
        }
        message = truncate(message, aiProperties.maxInputLength());
        promptGuard.inspect(message);

        List<Long> ids = request.pantryItemIds() == null ? List.of()
                : request.pantryItemIds().stream().filter(Objects::nonNull).distinct()
                .limit(aiProperties.maxPantryItems()).toList();
        List<PantryItem> pantryItems = pantryService.findOwnedByIds(userId, ids);
        pantryItems.forEach(item -> promptGuard.inspect(item.getName()));

        RecipePrompt prompt = promptFactory.build(message, pantryItems);
        GeneratedRecipe generated = aiClient.generate(prompt);
        validate(generated);

        Recipe recipe = new Recipe(userRepository.getReferenceById(userId));
        recipe.setTitle(truncate(generated.title(), 150));
        recipe.setSummary(truncate(generated.summary(), 600));
        recipe.setChefComment(truncate(generated.chefComment(), 600));
        recipe.setDifficulty(normalizeDifficulty(generated.difficulty()));
        recipe.setPrepTimeMinutes(clamp(generated.prepTimeMinutes(), 1, 1440));
        recipe.setServings(clamp(generated.servings(), 1, 50));
        recipe.setIngredients(limit(generated.ingredients(), 30, 200));
        recipe.setSteps(limit(generated.steps(), 25, 300));
        recipe.setTips(limit(generated.tips(), 5, 300));
        recipe.setUserRequest(truncate(message, 1000));
        return RecipeResponse.from(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> list(Long userId) {
        return recipeRepository.findByUserIdOrderByFavoriteDescCreatedAtDescIdDesc(userId).stream().map(RecipeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(Long userId, Long id) {
        return RecipeResponse.from(findOwned(userId, id));
    }

    @Transactional
    public RecipeResponse toggleFavorite(Long userId, Long id) {
        Recipe recipe = findOwned(userId, id);
        recipe.setFavorite(!recipe.isFavorite());
        return RecipeResponse.from(recipe);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        recipeRepository.delete(findOwned(userId, id));
    }

    private Recipe findOwned(Long userId, Long id) {
        return recipeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Receita não encontrada."));
    }

    /**
     * Last defense layer: the answer must be a complete recipe, otherwise it is discarded.
     */
    private void validate(GeneratedRecipe generated) {
        if (generated == null) {
            throw new AiResponseException(INVALID_ANSWER);
        }
        if (generated.offTopic()) {
            String reason = generated.refusalReason();
            throw new RecipeRefusedException(reason == null || reason.isBlank() ? DEFAULT_REFUSAL : truncate(reason, 300));
        }
        if (isBlank(generated.title()) || isEmpty(generated.ingredients()) || isEmpty(generated.steps())) {
            throw new AiResponseException(INVALID_ANSWER);
        }
    }

    private static String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return null;
        }
        String normalized = difficulty.trim().toUpperCase().replace("Á", "A").replace("É", "E").replace("Í", "I");
        return DIFFICULTIES.contains(normalized) ? normalized : null;
    }

    private static List<String> limit(List<String> values, int maxItems, int maxLength) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(value -> !isBlank(value))
                .map(value -> truncate(value.trim(), maxLength))
                .limit(maxItems)
                .toList();
    }

    private static Integer clamp(Integer value, int min, int max) {
        return value == null ? null : Math.max(min, Math.min(max, value));
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean isEmpty(List<String> values) {
        return values == null || values.stream().allMatch(RecipeService::isBlank);
    }
}
