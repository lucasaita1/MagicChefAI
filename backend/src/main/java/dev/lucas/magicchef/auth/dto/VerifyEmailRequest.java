package dev.lucas.magicchef.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank(message = "Token de confirmação ausente")
        @Size(max = 100, message = "Token inválido")
        String token
) {
}
