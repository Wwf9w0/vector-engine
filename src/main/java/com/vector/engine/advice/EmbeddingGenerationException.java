package com.vector.engine.advice;

public class EmbeddingGenerationException extends VectorEngineException {

    public static final String ERROR_CODE = "EMBEDDING_GENERATION_FAILED";

    protected EmbeddingGenerationException(String message, Throwable cause) {
        super(message, ERROR_CODE, cause);
    }

    public EmbeddingGenerationException(String message) {
        super(message, ERROR_CODE);
    }

    public static EmbeddingGenerationException connectionFailed(String endpoint, Throwable cause) {
        return new EmbeddingGenerationException(
                String.format("Failed to connect to embedding service at %s", endpoint),
                cause);
    }

    public static EmbeddingGenerationException timeout(int timeoutSeconds) {
        return new EmbeddingGenerationException(
                String.format("Embedding generation timed out after %d seconds", timeoutSeconds));
    }
}
