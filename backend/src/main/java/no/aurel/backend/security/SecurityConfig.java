package no.aurel.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // Customers need no account: they can browse, see free times and book. Everything else is for the admin (Agneta).
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginThrottle loginThrottle) throws Exception {
        http
                // Stateless HTTP Basic: the browser sends credentials on every request and no cookie is used,
                // so there is no session for an attacker to ride on and CSRF protection has nothing to protect.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/services", "/api/services/*",
                                "/api/employees", "/api/employees/*",
                                // Shown in the public footer; only the weekly hours are public, not the days off
                                "/api/employees/*/opening-hours",
                                "/api/add-ons", "/api/payment-options",
                                // The "Om oss" box on the public site, and its photo
                                "/api/about", "/api/about/image",
                                // The boxes with the treatments, and their photos
                                "/api/service-groups", "/api/service-groups/*/image",
                                "/api/availability").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll()
                        // The customer's browser asks whether its payment went through (the token is the secret)
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/check-payment").permitAll()
                        // Buttons on the fake payment page; the controller only exists when the fake gateway is on
                        .requestMatchers(HttpMethod.POST, "/api/dev/fake-vipps/*/*").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                // Plain 401 without a WWW-Authenticate header, so the browser does not pop up its own login box
                .httpBasic(basic -> basic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Refuses login attempts while the login is locked after repeated wrong passwords
                .addFilterBefore(new LoginThrottleFilter(loginThrottle), BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // One admin user, configured through properties/environment variables. The password is never stored in plain text
    // in memory beyond startup: it is hashed with BCrypt immediately.
    @Bean
    UserDetailsService userDetailsService(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password,
            PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("ADMIN")
                        .build());
    }
}
