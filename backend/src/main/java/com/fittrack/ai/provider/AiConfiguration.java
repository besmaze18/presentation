package com.fittrack.ai.provider;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.service.FoodAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Chooses the active {@link FoodAnalysisService} implementation from configuration. */
@Configuration
public class AiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiConfiguration.class);

    @Bean
    public FoodAnalysisService foodAnalysisService(
            AiProperties properties, FoodAnalysisResponseParser parser, ObjectMapper objectMapper) {

        if (!properties.isEnabled()) {
            log.info("AI food analysis is disabled (AI_ENABLED=false)");
            return new DisabledFoodAnalysisProvider();
        }

        if ("anthropic".equalsIgnoreCase(properties.getProvider())) {
            AiProperties.Anthropic config = properties.getAnthropic();
            if (!config.isConfigured()) {
                log.warn(
                        "AI provider 'anthropic' selected but ANTHROPIC_API_KEY is not set - "
                                + "AI analysis endpoints will return 503 and manual entry stays available");
                return new DisabledFoodAnalysisProvider();
            }
            AnthropicClient client = AnthropicOkHttpClient.builder()
                    .apiKey(config.getApiKey())
                    .baseUrl(config.getBaseUrl())
                    .timeout(config.getTimeout())
                    .build();
            log.info("AI food analysis enabled using provider 'anthropic', model {}", config.getModel());
            return new AnthropicFoodAnalysisProvider(client, config, parser, objectMapper);
        }

        log.warn("Unknown AI provider '{}' - AI analysis is disabled", properties.getProvider());
        return new DisabledFoodAnalysisProvider();
    }
}
