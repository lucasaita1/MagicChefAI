package dev.lucas.magicchef.pantry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PantryItemRepository extends JpaRepository<PantryItem, Long> {

    List<PantryItem> findByUserIdOrderByNameAsc(Long userId);

    List<PantryItem> findByUserIdAndLocationOrderByNameAsc(Long userId, StorageLocation location);

    Optional<PantryItem> findByIdAndUserId(Long id, Long userId);

    List<PantryItem> findByUserIdAndIdIn(Long userId, Collection<Long> ids);
}
