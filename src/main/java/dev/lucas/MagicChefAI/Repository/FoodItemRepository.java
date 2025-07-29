package dev.lucas.MagicChefAI.Repository;

import dev.lucas.MagicChefAI.Model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository <FoodItem, Long> {
}
