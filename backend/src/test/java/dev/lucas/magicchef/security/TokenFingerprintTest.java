package dev.lucas.magicchef.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenFingerprintTest {

    @Test
    void generatesRandomUrlSafeValues() {
        String first = TokenFingerprint.generate();

        assertThat(first).hasSize(43).matches("[A-Za-z0-9_-]+");
        assertThat(TokenFingerprint.generate()).isNotEqualTo(first);
    }

    @Test
    void hashIsDeterministicSha256Hex() {
        assertThat(TokenFingerprint.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void matchesOnlyTheOriginalFingerprint() {
        String fingerprint = TokenFingerprint.generate();
        String hash = TokenFingerprint.hash(fingerprint);

        assertThat(TokenFingerprint.matches(fingerprint, hash)).isTrue();
        assertThat(TokenFingerprint.matches("stolen-token-without-cookie", hash)).isFalse();
        assertThat(TokenFingerprint.matches(null, hash)).isFalse();
        assertThat(TokenFingerprint.matches(fingerprint, null)).isFalse();
    }
}
