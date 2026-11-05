package dev.lucas.magicchef.auth.dto;

public record UserResponse(Long id, String name, String email, boolean emailVerified) {
}
