package no.aurel.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginThrottleTest {

    // A clock the test can move forward, so waiting 30 minutes takes no time
    private static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-10-20T10:00:00Z");

        void advance(Duration d) {
            now = now.plus(d);
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

    private TestClock clock;
    private LoginThrottle throttle;

    @BeforeEach
    void setUp() {
        clock = new TestClock();
        throttle = new LoginThrottle(clock);
    }

    private void failTimes(int times) {
        for (int i = 0; i < times; i++) {
            throttle.recordFailure();
        }
    }

    @Test
    void fourWrongAttemptsDoNotLockTheLogin() {
        failTimes(4);

        assertThat(throttle.remainingLock()).isZero();
    }

    @Test
    void theFifthWrongAttemptLocksForOneMinute() {
        failTimes(5);

        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void theLockCountsDownAndThenEnds() {
        failTimes(5);

        clock.advance(Duration.ofSeconds(30));
        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofSeconds(30));

        clock.advance(Duration.ofSeconds(30));
        assertThat(throttle.remainingLock()).isZero();
    }

    @Test
    void fiveMoreWrongAttemptsAfterwardsLockForTenMinutes() {
        failTimes(5);
        clock.advance(Duration.ofMinutes(1));

        failTimes(5);

        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void thenThirtyMinutesAndNeverMoreThanThat() {
        failTimes(5);
        clock.advance(Duration.ofMinutes(1));
        failTimes(5);
        clock.advance(Duration.ofMinutes(10));

        failTimes(5);
        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(30));

        clock.advance(Duration.ofMinutes(30));
        failTimes(5);
        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(30));

        clock.advance(Duration.ofMinutes(30));
        failTimes(5);
        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void attemptsDuringALockDoNotMakeItLonger() {
        failTimes(5);

        failTimes(20);

        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void theCountOfFiveStartsOverAfterEachLock() {
        failTimes(5);
        clock.advance(Duration.ofMinutes(1));

        // four wrong attempts are not enough for the next lock
        failTimes(4);

        assertThat(throttle.remainingLock()).isZero();
    }

    @Test
    void aCorrectLoginResetsEverythingIncludingTheEscalation() {
        failTimes(5);
        clock.advance(Duration.ofMinutes(1));
        failTimes(4);

        throttle.recordSuccess();

        // the four earlier failures are forgotten, and the next lock is the short one again
        failTimes(4);
        assertThat(throttle.remainingLock()).isZero();
        failTimes(1);
        assertThat(throttle.remainingLock()).isEqualTo(Duration.ofMinutes(1));
    }
}
