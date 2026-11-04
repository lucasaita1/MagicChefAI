package dev.lucas.magicchef.common;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RateLimiterTest {

    private final MutableClock clock = new MutableClock();
    private final RateLimiter limiter = new RateLimiter(clock);

    @Test
    void allowsUpToTheLimitThenBlocks() {
        limiter.acquire("k", 2, "slow down");
        limiter.acquire("k", 2, "slow down");

        assertThatThrownBy(() -> limiter.acquire("k", 2, "slow down"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage("slow down");
    }

    @Test
    void opensANewWindowAfterOneMinute() {
        limiter.acquire("k", 1, "x");
        clock.advance(Duration.ofSeconds(61));

        assertThatCode(() -> limiter.acquire("k", 1, "x")).doesNotThrowAnyException();
    }

    @Test
    void keysAreIndependentAndCanBeReset() {
        limiter.acquire("a", 1, "x");
        assertThatCode(() -> limiter.acquire("b", 1, "x")).doesNotThrowAnyException();

        limiter.reset("a");
        assertThatCode(() -> limiter.acquire("a", 1, "x")).doesNotThrowAnyException();
    }

    @Test
    void defaultConstructorUsesSystemClock() {
        assertThatCode(() -> new RateLimiter().acquire("k", 1, "x")).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-03T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
