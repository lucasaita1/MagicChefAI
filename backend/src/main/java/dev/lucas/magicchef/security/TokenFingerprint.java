package dev.lucas.magicchef.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random value sent to the browser in an HttpOnly cookie while only its SHA-256 hash goes
 * inside the JWT. A token stolen from JavaScript, logs or the network is useless without
 * the cookie, which scripts cannot read.
 */
public final class TokenFingerprint {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenFingerprint() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String fingerprint) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(fingerprint.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Constant-time comparison between a raw fingerprint and the expected hash.
     */
    public static boolean matches(String fingerprint, String expectedHash) {
        if (fingerprint == null || expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(fingerprint).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
