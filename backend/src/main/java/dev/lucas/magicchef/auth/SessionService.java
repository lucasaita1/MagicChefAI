package dev.lucas.magicchef.auth;

import dev.lucas.magicchef.config.SecurityProperties;
import dev.lucas.magicchef.security.AuthenticatedUser;
import dev.lucas.magicchef.security.JwtService;
import dev.lucas.magicchef.security.TokenFingerprint;
import dev.lucas.magicchef.user.User;
import dev.lucas.magicchef.user.UserSession;
import dev.lucas.magicchef.user.UserSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Opens, validates and revokes server-side sessions bound to JWTs.
 */
@Slf4j
@Service
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final Clock clock;
    private final Duration ttl;

    public SessionService(UserSessionRepository sessionRepository, JwtService jwtService,
                          SecurityProperties properties, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.clock = clock;
        this.ttl = properties.jwt().ttl();
    }

    @Transactional
    public IssuedSession open(User user) {
        Instant now = clock.instant();
        String fingerprint = TokenFingerprint.generate();
        String fingerprintHash = TokenFingerprint.hash(fingerprint);
        UserSession session = sessionRepository.save(new UserSession(user, fingerprintHash, now, now.plus(ttl)));
        String token = jwtService.encode(user.getId(), session.getId(), fingerprintHash, now, session.getExpiresAt());
        return new IssuedSession(token, fingerprint, session.getExpiresAt(), ttl, user);
    }

    /**
     * Accepts a token only if it is well signed, not expired, its session is active and the
     * fingerprint cookie matches. A valid token presented without its fingerprint is treated
     * as stolen and its session is burned.
     */
    @Transactional
    public Optional<AuthenticatedUser> authenticate(String token, String fingerprint) {
        Jwt jwt;
        UUID sessionId;
        try {
            jwt = jwtService.decode(token);
            sessionId = UUID.fromString(jwt.getId());
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }

        Instant now = clock.instant();
        Optional<UserSession> found = sessionRepository.findById(sessionId)
                .filter(session -> session.isActive(now))
                .filter(session -> session.getUser().getId().toString().equals(jwt.getSubject()));
        if (found.isEmpty()) {
            return Optional.empty();
        }

        UserSession session = found.get();
        String expectedHash = jwt.getClaimAsString(JwtService.FINGERPRINT_CLAIM);
        if (!session.getFingerprintHash().equals(expectedHash) || !TokenFingerprint.matches(fingerprint, expectedHash)) {
            log.warn("JWT for session {} used without its fingerprint cookie; revoking session", sessionId);
            session.revoke(now);
            return Optional.empty();
        }

        User user = session.getUser();
        return Optional.of(new AuthenticatedUser(user.getId(), user.getName(), user.getEmail(),
                user.isEmailVerified(), session.getId()));
    }

    @Transactional
    public void revoke(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> session.revoke(clock.instant()));
    }

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void purgeExpiredSessions() {
        int removed = sessionRepository.deleteExpired(clock.instant());
        log.info("Purged {} expired sessions", removed);
    }

    public record IssuedSession(String token, String fingerprint, Instant expiresAt, Duration ttl, User user) {
    }
}
