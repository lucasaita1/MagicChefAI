package dev.lucas.MagicChefAI.Service;

import dev.lucas.MagicChefAI.Controller.FoodItemController;
import dev.lucas.MagicChefAI.Model.FoodItem;
import dev.lucas.MagicChefAI.Repository.FoodItemRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class FoodItemService {

    private FoodItemRepository repository;

    //Metodo criar
    public FoodItem criarItem (@RequestBody FoodItem foodItem){
        return repository.save(foodItem);
    }

    // Metodo Listar
    public List<FoodItem> listarItens (){
        return repository.findAll();
    }

    //Metodo Listar por ID
    public Optional<FoodItem> listarItensPorID(Long id) {
        return repository.findById(id);
    }

    //Metodo Atualizar
    public FoodItem atualizarItem (@RequestBody FoodItem foodItem, @PathVariable Long id){
        if (repository.existsById(id)){
            foodItem.setId(id);
            return repository.save(foodItem);
        }
        return null;
    }

    //Metodo Deletar
    public boolean deletarItem (@PathVariable Long id){
        if (repository.existsById(id)){
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
