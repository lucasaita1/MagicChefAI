package dev.lucas.magicchef.pantry;

import dev.lucas.magicchef.pantry.dto.PantryItemRequest;
import dev.lucas.magicchef.pantry.dto.PantryItemResponse;
import dev.lucas.magicchef.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Geladeira e armário")
@RestController
@RequestMapping("/api/pantry")
@RequiredArgsConstructor
public class PantryController {

    private final PantryService pantryService;

    @Operation(summary = "Lista os ingredientes (opcionalmente filtrando por FRIDGE ou CUPBOARD)")
    @GetMapping
    public List<PantryItemResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                         @RequestParam(required = false) StorageLocation location) {
        return pantryService.list(user.id(), location);
    }

    @Operation(summary = "Cadastra um ingrediente")
    @PostMapping
    public ResponseEntity<PantryItemResponse> create(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @Valid @RequestBody PantryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pantryService.create(user.id(), request));
    }

    @Operation(summary = "Atualiza um ingrediente")
    @PutMapping("/{id}")
    public PantryItemResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                                     @PathVariable Long id,
                                     @Valid @RequestBody PantryItemRequest request) {
        return pantryService.update(user.id(), id, request);
    }

    @Operation(summary = "Remove um ingrediente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        pantryService.delete(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
