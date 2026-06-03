package com.vector.engine.client;

import com.vector.engine.advice.EmbeddingGenerationException;
import com.vector.engine.config.OllamProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.mode", havingValue = "real")
public class OllamaEmbeddingClient implements EmbeddingService {

    private final RestClient restClient;
    private final OllamProperties properties;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public OllamaEmbeddingClient(OllamProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();

        log.info("🤖 OllamaEmbeddingClient initialized | URL: {} | Model: {} | Dimensions: {} | Timeout: {}s",
                properties.baseUrl(),
                properties.model(),
                properties.embeddingDimensions(),
                properties.timeoutSeconds());
        log.info(" Retry config | MaxAttempts: {} | InitialDelay: {}ms | Multiplier: {}",
                properties.retry().maxAttempts(),
                properties.retry().initialDelayMs(),
                properties.retry().multiplier());
    }

    @Override
    public CompletableFuture<List<Float>> generateEmbedding(String text) {
        return CompletableFuture.supplyAsync(
                () -> generateEmbeddingSync(text),
                virtualThreadExecutor);
    }

    @Override
    @Retryable(retryFor = {RestClientException.class,
            ResourceAccessException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public List<Float> generateEmbeddingSync(String text) {
        Instant startTıme = Instant.now();
        log.debug("Generating embedding | Text length: {}, chars | Model: {}",
                text.length(), properties.model());

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", properties.model(),
                    "prompt", text);

            Map<String, Object> response = restClient.post()
                    .uri("/api/embeddings")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("embedding")) {
                throw new EmbeddingGenerationException(
                        "Invalid Ollama response: missing 'embedding' field");
            }

            List<Number> rawEmbedding = (List<Number>) response.get("embedding");
            List<Float> embedding = rawEmbedding.stream()
                    .map(Number::floatValue)
                    .toList();

            Duration duration = Duration.between(startTıme, Instant.now());
            log.debug("Embedding generated | Dimensions: {} | Time: {}ms",
                    embedding.size(), duration.toMillis());

            if (embedding.size() != properties.embeddingDimensions()) {
                log.warn("Dimension mismatch! Expected: {}, Got:{}",
                        properties.embeddingDimensions(), embedding.size());
            }

            return embedding;

        } catch (RestClientException e) {
            Duration duration = Duration.between(startTıme, Instant.now());
            log.warn("Ollama request failed | Time: {}ms | Error: {} | Will retry if attempts remain",
                    duration.toMillis(), e.getMessage());
            throw e;
        }
    }

    @Override
    public int getEmbeddingDimensions() {
        return properties.embeddingDimensions();
    }

    @Override
    public boolean isHealthy() {
        try {
            restClient.get()
                    .uri("/")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Ollama health check failed: {}", e.getMessage());
            return false;
        }
    }
}
