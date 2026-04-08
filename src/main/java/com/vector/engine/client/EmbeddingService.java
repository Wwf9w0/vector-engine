package com.vector.engine.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EmbeddingService {

    CompletableFuture<List<Float>> generateEmbedding(String text);

    List<Float> generateEmbeddingSync(String text);

    int getEmbeddingDimensions();

    boolean isHealthy();
}
