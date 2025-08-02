package dev.lucas.MagicChefAI.Controller;

import dev.lucas.MagicChefAI.Model.FoodItem;
import dev.lucas.MagicChefAI.Service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    @PostMapping("/criar")
    public ResponseEntity<FoodItem> criandoItem(@RequestBody FoodItem foodItem) {
        return ResponseEntity.status(HttpStatus.CREATED).body(foodItemService.criarItem(foodItem));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<FoodItem>> listandoItens() {
        return ResponseEntity.ok(foodItemService.listarItens());
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<FoodItem> listandoPorId(@PathVariable Long id) {
        return foodItemService.listarItensPorID(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<FoodItem> atualizandoItem(@RequestBody FoodItem foodItem, @PathVariable Long id) {
        Optional<FoodItem> existente = foodItemService.listarItensPorID(id);
        if (existente.isPresent()) {
            FoodItem itemAtualizado = foodItemService.atualizarItem(foodItem, id);
            return ResponseEntity.ok(itemAtualizado);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletandoItem(@PathVariable Long id) {
        foodItemService.deletarItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deletar/todos")
    public ResponseEntity<Void> deletandoTudo(){
        foodItemService.deleteall();
        return ResponseEntity.noContent().build();
    }
}

