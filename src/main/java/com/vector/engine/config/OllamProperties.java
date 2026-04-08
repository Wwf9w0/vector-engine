package com.vector.engine.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ollama")
public record OllamProperties(

        @NotBlank(message = "Ollama base Url is required")
        String baseUrl,

        @NotBlank(message = "Ollama model name is required")
        String model,

        @Positive(message = "Embedding dimensions must be possitive")
        int embeddingDimensions,
        @Min(value = 1, message = "Timeout must be at least 1 second") @Max(value = 300, message = "Timeout cannot exceed 300 seconds")
        int timeoutSeconds,

        RetryConfig retry) {

    public OllamProperties{
        if (timeoutSeconds == 0) {
            timeoutSeconds = 30;
        }
        if (retry == null) {
            retry = new RetryConfig(3, 1000, 2.0);
        }
    }


        public record RetryConfig(
                @Min(1) @Max(10)
                int maxAttempts,
                @Min(100) @Max(60000)
                long initialDelayMs,
                @Min(1) @Max(5)
                double multiplier) {

            public RetryConfig{
                if (maxAttempts == 0) {
                    maxAttempts = 3;
                }
                if (initialDelayMs == 0) {
                    initialDelayMs = 1000;
                }
                if (multiplier == 0) {
                    multiplier = 2.0;
                }
            }
        }
}
