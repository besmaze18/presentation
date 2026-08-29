package com.fittrack.support;

import com.fittrack.ai.service.FoodAnalysisService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AiTestConfiguration {

    @Bean
    @Primary
    public FoodAnalysisService stubFoodAnalysisService() {
        return new StubFoodAnalysisService();
    }
}
