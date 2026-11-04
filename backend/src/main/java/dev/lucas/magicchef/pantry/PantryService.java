package dev.lucas.magicchef.pantry;

import dev.lucas.magicchef.ai.PromptGuard;
import dev.lucas.magicchef.common.NotFoundException;
import dev.lucas.magicchef.pantry.dto.PantryItemRequest;
import dev.lucas.magicchef.pantry.dto.PantryItemResponse;
import dev.lucas.magicchef.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Manages the user's fridge and cupboard. Every query is scoped by the authenticated user,
 * so nobody can read or change someone else's ingredients.
 */
@Service
@RequiredArgsConstructor
public class PantryService {

    private final PantryItemRepository repository;
    private final UserRepository userRepository;
    private final PromptGuard promptGuard;

    @Transactional(readOnly = true)
    public List<PantryItemResponse> list(Long userId, StorageLocation location) {
        List<PantryItem> items = location == null
                ? repository.findByUserIdOrderByNameAsc(userId)
                : repository.findByUserIdAndLocationOrderByNameAsc(userId, location);
        return items.stream().map(PantryItemResponse::from).toList();
    }

    @Transactional
    public PantryItemResponse create(Long userId, PantryItemRequest request) {
        PantryItem item = new PantryItem(userRepository.getReferenceById(userId));
        apply(item, request);
        return PantryItemResponse.from(repository.save(item));
    }

    @Transactional
    public PantryItemResponse update(Long userId, Long id, PantryItemRequest request) {
        PantryItem item = findOwned(userId, id);
        apply(item, request);
        return PantryItemResponse.from(item);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        repository.delete(findOwned(userId, id));
    }

    @Transactional(readOnly = true)
    public List<PantryItem> findOwnedByIds(Long userId, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findByUserIdAndIdIn(userId, ids);
    }

    private PantryItem findOwned(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Ingrediente não encontrado."));
    }

    private void apply(PantryItem item, PantryItemRequest request) {
        // Ingredient names end up inside AI prompts, so they go through the same guard as chat input
        String name = promptGuard.sanitize(request.name());
        promptGuard.inspect(name);
        item.setName(name);
        item.setCategory(request.category());
        item.setLocation(request.location());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit());
        item.setExpiresAt(request.expiresAt());
    }
}
