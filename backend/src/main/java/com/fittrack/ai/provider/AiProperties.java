package com.fittrack.ai.provider;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fittrack.ai")
public class AiProperties {

    /** Selects the active provider adapter. Only "anthropic" ships today. */
    private String provider = "anthropic";

    private boolean enabled = true;

    private Anthropic anthropic = new Anthropic();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Anthropic getAnthropic() {
        return anthropic;
    }

    public void setAnthropic(Anthropic anthropic) {
        this.anthropic = anthropic;
    }

    public static class Anthropic {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com";
        private String model = "claude-opus-5";
        private long maxTokens = 4096;
        private Duration timeout = Duration.ofSeconds(60);

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public long getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(long maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
