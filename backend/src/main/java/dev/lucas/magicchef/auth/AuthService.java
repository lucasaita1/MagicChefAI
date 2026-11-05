package dev.lucas.magicchef.auth;

import dev.lucas.magicchef.auth.dto.LoginRequest;
import dev.lucas.magicchef.auth.dto.RegisterRequest;
import dev.lucas.magicchef.auth.dto.UserResponse;
import dev.lucas.magicchef.common.ConflictException;
import dev.lucas.magicchef.common.NotFoundException;
import dev.lucas.magicchef.common.RateLimiter;
import dev.lucas.magicchef.config.SecurityProperties;
import dev.lucas.magicchef.mail.EmailDeliveryException;
import dev.lucas.magicchef.security.AuthenticatedUser;
import dev.lucas.magicchef.user.User;
import dev.lucas.magicchef.user.UserRepository;
import dev.lucas.magicchef.verification.DisposableEmailPolicy;
import dev.lucas.magicchef.verification.EmailVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final EmailVerificationService verificationService;
    private final DisposableEmailPolicy disposableEmailPolicy;
    private final RateLimiter rateLimiter;
    private final SecurityProperties properties;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, SessionService sessionService,
                       EmailVerificationService verificationService, DisposableEmailPolicy disposableEmailPolicy,
                       RateLimiter rateLimiter, SecurityProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.sessionService = sessionService;
        this.verificationService = verificationService;
        this.disposableEmailPolicy = disposableEmailPolicy;
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    /**
     * Creates the account, e-mails the confirmation link and opens a session. The user can browse
     * right away, but the AI stays locked until the e-mail is confirmed.
     */
    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public SessionService.IssuedSession register(RegisterRequest request, String clientIp) {
        rateLimiter.acquire("register-ip:" + clientIp, properties.registerIpAttemptsPerMinute(),
                "Muitas contas criadas a partir desta rede. Aguarde um minuto e tente novamente.");
        String email = normalize(request.email());
        disposableEmailPolicy.check(email);
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }
        User user = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        try {
            verificationService.sendVerification(user);
        } catch (EmailDeliveryException e) {
            // The account is still created; the user can ask for a new link from the app
            log.warn("Verification e-mail for user {} could not be sent", user.getId());
        }
        return sessionService.open(user);
    }

    /**
     * Two rate limits: per IP (stops one machine from spraying many accounts) and per IP + e-mail
     * (stops brute force on one account). Credentials are checked by Spring Security's
     * {@link AuthenticationManager}, which mitigates timing attacks.
     */
    @Transactional
    public SessionService.IssuedSession login(LoginRequest request, String clientIp) {
        String email = normalize(request.email());
        rateLimiter.acquire("login-ip:" + clientIp, properties.loginIpAttemptsPerMinute(),
                "Muitas tentativas de login a partir desta rede. Aguarde um minuto e tente novamente.");
        String accountKey = "login:" + clientIp + ":" + email;
        rateLimiter.acquire(accountKey, properties.loginAttemptsPerMinute(),
                "Muitas tentativas de login. Aguarde um minuto e tente novamente.");

        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        rateLimiter.reset(accountKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        return sessionService.open(user);
    }

    public void logout(AuthenticatedUser principal) {
        sessionService.revoke(principal.sessionId());
    }

    public UserResponse me(AuthenticatedUser principal) {
        return new UserResponse(principal.id(), principal.name(), principal.email(), principal.emailVerified());
    }

    public UserResponse verifyEmail(String token) {
        User user = verificationService.verify(token);
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), true);
    }

    public void resendVerification(AuthenticatedUser principal) {
        verificationService.resend(principal.id());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
