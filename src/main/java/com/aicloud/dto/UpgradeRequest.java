package com.aicloud.dto;

/** Cuerpo de POST /api/quota/upgrade. El pago es simulado: la tarjeta no se guarda. */
public record UpgradeRequest(String plan, String cardHolder, String cardNumber, String expiry, String cvv) {
}
