package dev.lucas.magicchef.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Informe sua senha")
        @Size(max = 72, message = "Senha muito longa")
        String password
) {
}
