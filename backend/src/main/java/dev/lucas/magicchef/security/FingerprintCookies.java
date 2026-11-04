package dev.lucas.magicchef.security;

import dev.lucas.magicchef.config.SecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.Optional;

@Component
public class FingerprintCookies {

    private static final String PATH = "/api";

    private final SecurityProperties.Cookie settings;

    public FingerprintCookies(SecurityProperties properties) {
        this.settings = properties.cookie();
    }

    public ResponseCookie create(String fingerprint, Duration maxAge) {
        return base(fingerprint).maxAge(maxAge).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(Duration.ZERO).build();
    }

    public Optional<String> read(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, settings.name()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank());
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(settings.name(), value)
                .httpOnly(true)
                .secure(settings.secure())
                .sameSite(settings.sameSite())
                .path(PATH);
    }
}
