package no.aurel.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Slows down password guessing on the admin login.
 *
 * After 5 wrong attempts the login is locked for 1 minute. After the lock ends, 5 more wrong attempts lock it for
 * 10 minutes, and 5 more after that for 30 minutes. From then on every lock is 30 minutes. A correct login starts
 * everything over.
 *
 * There is only one admin, so there is one counter for the whole login (not one per address). That is the
 * strictest choice: guessing from many addresses does not help an attacker. The price is that someone could keep
 * the admin locked out on purpose, but they still cannot get in, and customers are not affected.
 *
 * The state is kept in memory, so it starts over when the application restarts. That is fine for a single instance.
 */
@Component
public class LoginThrottle {

    static final int ATTEMPTS_BEFORE_LOCK = 5;

    // The 1st, 2nd and 3rd lock. Every lock after that uses the last value.
    private static final List<Duration> LOCK_STEPS = List.of(Duration.ofMinutes(1), Duration.ofMinutes(10), Duration.ofMinutes(30));

    private final Clock clock;

    private int failures;
    private int locksServed;
    private Instant lockedUntil = Instant.MIN;

    public LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    // How much longer the login is locked. Zero when it is open.
    public synchronized Duration remainingLock() {
        Instant now = clock.instant();
        return now.isBefore(lockedUntil) ? Duration.between(now, lockedUntil) : Duration.ZERO;
    }

    public synchronized void recordFailure() {
        if (!remainingLock().isZero()) {
            return;
        }
        failures++;
        if (failures >= ATTEMPTS_BEFORE_LOCK) {
            locksServed++;
            Duration lock = LOCK_STEPS.get(Math.min(locksServed, LOCK_STEPS.size()) - 1);
            lockedUntil = clock.instant().plus(lock);
            failures = 0;
        }
    }

    public synchronized void recordSuccess() {
        failures = 0;
        locksServed = 0;
        lockedUntil = Instant.MIN;
    }
}
