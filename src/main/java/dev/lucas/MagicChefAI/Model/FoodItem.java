package dev.lucas.MagicChefAI.Model;


import dev.lucas.MagicChefAI.Enums.FoodItemCategorys;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Enumerated(EnumType.STRING)
    private FoodItemCategorys categoria;
    private int quantidade;
    private LocalDate validade;
}
