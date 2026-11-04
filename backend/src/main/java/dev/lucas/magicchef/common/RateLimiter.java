package dev.lucas.magicchef.common;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory fixed-window rate limiter. Good enough for a single instance;
 * protects the OpenAI bill and slows down brute-force logins.
 */
@Component
public class RateLimiter {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter() {
        this(Clock.systemUTC());
    }

    RateLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * Registers one hit for {@code key} and throws when it exceeds {@code limit} per minute.
     */
    public void acquire(String key, int limit, String message) {
        long now = clock.millis();
        Window window = windows.compute(key, (k, current) ->
                current == null || now - current.startedAt() >= WINDOW.toMillis()
                        ? new Window(now, 1)
                        : new Window(current.startedAt(), current.hits() + 1));
        if (window.hits() > limit) {
            throw new TooManyRequestsException(message);
        }
    }

    public void reset(String key) {
        windows.remove(key);
    }

    private record Window(long startedAt, int hits) {
    }
}
