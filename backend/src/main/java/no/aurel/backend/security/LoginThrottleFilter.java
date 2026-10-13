package no.aurel.backend.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Stops login attempts while the login is locked. It runs BEFORE the password is checked, so even the correct
 * password is refused during a lock (otherwise an attacker could keep guessing and simply ignore the 401s).
 *
 * Only requests that carry login credentials are affected. Customers browsing and booking send none, so they
 * are never blocked.
 */
public class LoginThrottleFilter extends OncePerRequestFilter {

    private final LoginThrottle throttle;

    public LoginThrottleFilter(LoginThrottle throttle) {
        this.throttle = throttle;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        boolean carriesLogin = authorization != null && authorization.regionMatches(true, 0, "Basic ", 0, 6);
        Duration remaining = throttle.remainingLock();

        if (carriesLogin && !remaining.isZero()) {
            long seconds = Math.max(1, (remaining.toMillis() + 999) / 1000);
            long minutes = (seconds + 59) / 60;
            String message = "For mange feil forsøk. Prøv igjen om " + minutes + (minutes == 1 ? " minutt." : " minutter.");

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(seconds));
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\",\"retryAfterSeconds\":" + seconds + "}");
            return;
        }
        chain.doFilter(request, response);
    }
}
