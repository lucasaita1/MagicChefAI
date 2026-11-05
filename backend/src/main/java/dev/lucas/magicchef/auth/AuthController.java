package dev.lucas.magicchef.auth;

import dev.lucas.magicchef.auth.dto.AuthResponse;
import dev.lucas.magicchef.auth.dto.LoginRequest;
import dev.lucas.magicchef.auth.dto.RegisterRequest;
import dev.lucas.magicchef.auth.dto.UserResponse;
import dev.lucas.magicchef.auth.dto.VerifyEmailRequest;
import dev.lucas.magicchef.security.AuthenticatedUser;
import dev.lucas.magicchef.security.ClientIpResolver;
import dev.lucas.magicchef.security.FingerprintCookies;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FingerprintCookies fingerprintCookies;
    private final ClientIpResolver clientIpResolver;

    @Operation(summary = "Cria uma conta, envia o e-mail de confirmação e já inicia a sessão")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return withSession(HttpStatus.CREATED, authService.register(request, clientIpResolver.resolve(http)));
    }

    @Operation(summary = "Autentica com e-mail e senha")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return withSession(HttpStatus.OK, authService.login(request, clientIpResolver.resolve(http)));
    }

    @Operation(summary = "Encerra a sessão: o token deixa de funcionar imediatamente")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.logout(user);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, fingerprintCookies.clear().toString())
                .build();
    }

    @Operation(summary = "Dados do usuário autenticado")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.me(user);
    }

    @Operation(summary = "Confirma o e-mail com o token recebido por e-mail")
    @PostMapping("/verify-email")
    public UserResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request.token());
    }

    @Operation(summary = "Reenvia o e-mail de confirmação (1 por minuto)")
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.resendVerification(user);
        return ResponseEntity.accepted().build();
    }

    private ResponseEntity<AuthResponse> withSession(HttpStatus status, SessionService.IssuedSession session) {
        AuthResponse body = new AuthResponse(session.token(), session.expiresAt(),
                new UserResponse(session.user().getId(), session.user().getName(), session.user().getEmail(),
                        session.user().isEmailVerified()));
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, fingerprintCookies.create(session.fingerprint(), session.ttl()).toString())
                .body(body);
    }
}
