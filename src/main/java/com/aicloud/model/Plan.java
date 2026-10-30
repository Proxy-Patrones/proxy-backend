package com.aicloud.model;

/**
 * Planes de suscripcion. -1 significa ilimitado.
 */
public enum Plan {

    FREE(10, 50_000L, 0),
    PRO(60, 500_000L, 20),
    ENTERPRISE(-1, -1L, 99);

    private final int requestsPerMinute;
    private final long monthlyTokens;
    private final int priceUsd;

    Plan(int requestsPerMinute, long monthlyTokens, int priceUsd) {
        this.requestsPerMinute = requestsPerMinute;
        this.monthlyTokens = monthlyTokens;
        this.priceUsd = priceUsd;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public long getMonthlyTokens() {
        return monthlyTokens;
    }

    public int getPriceUsd() {
        return priceUsd;
    }

    public boolean isUnlimited() {
        return requestsPerMinute < 0;
    }

    /** Solo se permite subir de plan, nunca bajar. */
    public boolean canUpgradeTo(Plan target) {
        return target.ordinal() > this.ordinal();
    }
}
