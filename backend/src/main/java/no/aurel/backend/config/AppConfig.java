package no.aurel.backend.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class AppConfig {

    // The salon is in Norway. Cloud servers run in UTC, so "now" must be asked for in Oslo time explicitly,
    // otherwise "this time has already passed" would be wrong by 1-2 hours. Tests can supply a fixed Clock.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Oslo"));
    }
}
