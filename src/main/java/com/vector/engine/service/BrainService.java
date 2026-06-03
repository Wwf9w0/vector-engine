package com.vector.engine.service;

import com.vector.engine.client.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrainService {

    private final EmbeddingService embeddingService;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public CompletableFuture<String> createEmbedding(String content) {
        final Instant startTime = Instant.now();
        return embeddingService.generateEmbedding(content)
                .thenApplyAsync(embedding -> {
                    Duration embeddingDuration = Duration.between(startTime, Instant.now());
                    log.info("Embedding generated | Dimensions: {} | Time: {}ms",
                            embedding.size(), embeddingDuration.toMillis());
                    Duration totalDuration = Duration.between(startTime, Instant.now());
                    log.info("Embedding created succesfully | Total time: {}ms", totalDuration.toMillis());
                    return embeddingToString(embedding);
                }, virtualThreadExecutor)
                .exceptionally(thtowable -> {
                    Duration failDuration = Duration.between(startTime, Instant.now());
                    log.error("Embedding creation failed | Time: {}ms, | Error: {}",
                            failDuration.toMillis(), thtowable.getMessage());
                    throw new RuntimeException(thtowable);
                });
    }

    private String embeddingToString(List<Float> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(embedding.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
