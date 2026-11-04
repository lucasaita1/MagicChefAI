package dev.lucas.magicchef.security;

import dev.lucas.magicchef.auth.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests carrying {@code Authorization: Bearer <jwt>}. The token is only
 * accepted when its signature is valid, its session was not revoked and the fingerprint
 * cookie matches. Otherwise the request stays anonymous and protected routes answer 401.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final SessionService sessionService;
    private final FingerprintCookies fingerprintCookies;

    public JwtAuthenticationFilter(SessionService sessionService, FingerprintCookies fingerprintCookies) {
        this.sessionService = sessionService;
        this.fingerprintCookies = fingerprintCookies;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            String fingerprint = fingerprintCookies.read(request).orElse(null);
            sessionService.authenticate(token, fingerprint).ifPresent(user -> {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, AUTHORITIES));
                SecurityContextHolder.setContext(context);
            });
        }
        chain.doFilter(request, response);
    }
}
