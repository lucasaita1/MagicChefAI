package dev.lucas.magicchef.verification;

import dev.lucas.magicchef.common.BadRequestException;
import dev.lucas.magicchef.common.NotFoundException;
import dev.lucas.magicchef.common.RateLimiter;
import dev.lucas.magicchef.config.MailProperties;
import dev.lucas.magicchef.mail.EmailDeliveryException;
import dev.lucas.magicchef.mail.EmailSender;
import dev.lucas.magicchef.mail.VerificationEmailTemplate;
import dev.lucas.magicchef.security.TokenFingerprint;
import dev.lucas.magicchef.user.User;
import dev.lucas.magicchef.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Issues single-use, expiring e-mail confirmation links and consumes them.
 */
@Service
public class EmailVerificationService {

    private static final int RESENDS_PER_MINUTE = 1;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final VerificationEmailTemplate template;
    private final RateLimiter rateLimiter;
    private final MailProperties properties;
    private final Clock clock;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository,
                                    EmailSender emailSender, VerificationEmailTemplate template,
                                    RateLimiter rateLimiter, MailProperties properties, Clock clock) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.template = template;
        this.rateLimiter = rateLimiter;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * Invalidates previous links, stores the hash of a fresh random token and e-mails the raw one.
     */
    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public void sendVerification(User user) {
        Instant now = clock.instant();
        tokenRepository.invalidateActive(user.getId(), now);
        // Same 256-bit random generator and SHA-256 hashing used for session fingerprints
        String rawToken = TokenFingerprint.generate();
        tokenRepository.save(new EmailVerificationToken(user, TokenFingerprint.hash(rawToken), now,
                now.plus(properties.verificationTtl())));
        String link = properties.appBaseUrl().replaceAll("/+$", "") + "/verificar-email?token=" + rawToken;
        emailSender.send(template.build(user.getEmail(), user.getName(), link));
    }

    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public void resend(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        if (user.isEmailVerified()) {
            return;
        }
        rateLimiter.acquire("verify-resend:" + userId, RESENDS_PER_MINUTE,
                "Aguarde um minuto antes de pedir um novo e-mail de confirmação.");
        sendVerification(user);
    }

    @Transactional
    public User verify(String rawToken) {
        Instant now = clock.instant();
        EmailVerificationToken token = tokenRepository.findByTokenHash(TokenFingerprint.hash(rawToken))
                .filter(candidate -> candidate.isUsable(now))
                .orElseThrow(() -> new BadRequestException("Link de confirmação inválido ou expirado. Peça um novo e-mail."));
        token.consume(now);
        User user = token.getUser();
        user.markEmailVerified(now);
        return user;
    }
}
