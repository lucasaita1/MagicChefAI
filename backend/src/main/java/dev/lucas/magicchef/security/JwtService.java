package dev.lucas.magicchef.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import dev.lucas.magicchef.config.SecurityProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Signs and verifies HS256 JWTs with Spring Security's Nimbus integration.
 */
@Service
public class JwtService {

    public static final String FINGERPRINT_CLAIM = "fgp";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final String issuer;

    public JwtService(SecurityProperties properties) {
        SecretKey key = new SecretKeySpec(
                properties.jwt().secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.issuer = properties.jwt().issuer();
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        nimbusDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        this.decoder = nimbusDecoder;
    }

    public String encode(Long userId, UUID sessionId, String fingerprintHash, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .id(sessionId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(FINGERPRINT_CLAIM, fingerprintHash)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /**
     * Verifies signature, issuer and expiration.
     *
     * @throws JwtException when the token is invalid
     */
    public Jwt decode(String token) {
        return decoder.decode(token);
    }
}
