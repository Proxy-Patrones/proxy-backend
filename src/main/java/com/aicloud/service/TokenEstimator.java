package com.aicloud.service;

/**
 * Aproximacion usada por la mayoria de proveedores: 1 token ~ 4 caracteres.
 * El frontend usa la misma formula para el estimador.
 */
public final class TokenEstimator {

    private TokenEstimator() {
    }

    public static int estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(text.trim().length() / 4.0));
    }
}
