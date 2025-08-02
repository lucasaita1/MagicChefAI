package dev.lucas.MagicChefAI.Service;

import dev.lucas.MagicChefAI.Controller.FoodItemController;
import dev.lucas.MagicChefAI.Model.FoodItem;
import dev.lucas.MagicChefAI.Repository.FoodItemRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodItemService {

    private final FoodItemRepository repository;

    public FoodItem criarItem(FoodItem foodItem) {
        return repository.save(foodItem);
    }

    public List<FoodItem> listarItens() {
        return repository.findAll();
    }

    public Optional<FoodItem> listarItensPorID(Long id) {
        return repository.findById(id);
    }

    public FoodItem atualizarItem(FoodItem foodItem, Long id) {
        if (repository.existsById(id)) {
            foodItem.setId(id);
            return repository.save(foodItem);
        }
        return null;
    }

    public boolean deletarItem(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public void deleteall(){
        repository.deleteAll();
    }
}
