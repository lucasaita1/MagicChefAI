package dev.lucas.magicchef.security;

import java.util.UUID;

/**
 * Principal stored in the security context after a JWT passes every check.
 */
public record AuthenticatedUser(Long id, String name, String email, boolean emailVerified, UUID sessionId) {
}
