package com.aicloud.exception;

/** Se lanza cuando el usuario agota su cuota mensual de tokens (HTTP 402). */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }
}
