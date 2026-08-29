package com.fittrack.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    /** Injected everywhere instead of Instant.now(), so time-dependent logic stays testable. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
