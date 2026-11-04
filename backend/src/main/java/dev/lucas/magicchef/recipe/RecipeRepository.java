package dev.lucas.magicchef.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Recipe> findByIdAndUserId(Long id, Long userId);
}
