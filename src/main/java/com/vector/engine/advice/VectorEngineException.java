package com.vector.engine.advice;

public class VectorEngineException extends RuntimeException {

    private final String errorCode;

    protected VectorEngineException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }


    protected VectorEngineException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
