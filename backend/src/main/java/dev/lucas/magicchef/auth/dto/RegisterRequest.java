package dev.lucas.magicchef.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Informe seu nome")
        @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres")
        String name,

        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "E-mail muito longo")
        String email,

        @NotBlank(message = "Informe uma senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "A senha precisa ter letras e números")
        String password
) {
}
