package dev.lucas.magicchef.auth;

import dev.lucas.magicchef.auth.dto.LoginRequest;
import dev.lucas.magicchef.auth.dto.RegisterRequest;
import dev.lucas.magicchef.auth.dto.UserResponse;
import dev.lucas.magicchef.common.ConflictException;
import dev.lucas.magicchef.common.NotFoundException;
import dev.lucas.magicchef.common.RateLimiter;
import dev.lucas.magicchef.config.SecurityProperties;
import dev.lucas.magicchef.security.AuthenticatedUser;
import dev.lucas.magicchef.user.User;
import dev.lucas.magicchef.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final RateLimiter rateLimiter;
    private final int loginAttemptsPerMinute;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, SessionService sessionService,
                       RateLimiter rateLimiter, SecurityProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.sessionService = sessionService;
        this.rateLimiter = rateLimiter;
        this.loginAttemptsPerMinute = properties.loginAttemptsPerMinute();
    }

    @Transactional
    public SessionService.IssuedSession register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }
        User user = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        return sessionService.open(user);
    }

    /**
     * Delegates credential checking to Spring Security's {@link AuthenticationManager}, which
     * throws {@code BadCredentialsException} with a generic message and mitigates timing attacks.
     */
    @Transactional
    public SessionService.IssuedSession login(LoginRequest request, String clientIp) {
        String email = normalize(request.email());
        String limiterKey = "login:" + clientIp + ":" + email;
        rateLimiter.acquire(limiterKey, loginAttemptsPerMinute,
                "Muitas tentativas de login. Aguarde um minuto e tente novamente.");

        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        rateLimiter.reset(limiterKey);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        return sessionService.open(user);
    }

    public void logout(AuthenticatedUser principal) {
        sessionService.revoke(principal.sessionId());
    }

    public UserResponse me(AuthenticatedUser principal) {
        return new UserResponse(principal.id(), principal.name(), principal.email());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
