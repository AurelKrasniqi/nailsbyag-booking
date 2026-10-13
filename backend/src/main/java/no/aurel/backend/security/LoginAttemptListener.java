package no.aurel.backend.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Spring Security announces every login result. This only counts: a wrong password adds to the counter,
// a correct one resets it. The blocking itself is done by LoginThrottleFilter.
@Component
@RequiredArgsConstructor
public class LoginAttemptListener {

    private final LoginThrottle throttle;

    @EventListener
    void onWrongPassword(AuthenticationFailureBadCredentialsEvent event) {
        throttle.recordFailure();
    }

    @EventListener
    void onCorrectLogin(AuthenticationSuccessEvent event) {
        throttle.recordSuccess();
    }
}
