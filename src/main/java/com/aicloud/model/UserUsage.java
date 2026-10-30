package com.aicloud.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Contadores de consumo de un usuario.
 * Todos los metodos son synchronized para que dos requests simultaneas
 * no puedan consumir el mismo cupo al mismo tiempo.
 */
public class UserUsage {

    private int requestsThisMinute;
    private long tokensUsedThisMonth;
    private final Map<LocalDate, DailyUsage> history = new HashMap<>();

    /** Intenta registrar una request. Devuelve false si ya llego al limite por minuto. */
    public synchronized boolean tryAcquireRequest(int limitPerMinute) {
        if (limitPerMinute >= 0 && requestsThisMinute >= limitPerMinute) {
            return false;
        }
        requestsThisMinute++;
        return true;
    }

    /** Reserva tokens antes de llamar al servicio real. Devuelve false si no alcanza la cuota. */
    public synchronized boolean tryReserveTokens(long tokens, long monthlyLimit) {
        if (monthlyLimit >= 0) {
            long remaining = monthlyLimit - tokensUsedThisMonth;
            if (remaining <= 0 || tokens > remaining) {
                return false;
            }
        }
        tokensUsedThisMonth += tokens;
        return true;
    }

    /** Ajusta la reserva con el consumo real y lo guarda en el historial del dia. */
    public synchronized void confirmTokens(long reserved, long realTokens, LocalDate day) {
        tokensUsedThisMonth += (realTokens - reserved);
        history.computeIfAbsent(day, d -> new DailyUsage(d, 0, 0)).add(1, realTokens);
    }

    /** Devuelve la reserva si el servicio real fallo. */
    public synchronized void releaseTokens(long reserved) {
        tokensUsedThisMonth = Math.max(0, tokensUsedThisMonth - reserved);
    }

    public synchronized void resetMinute() {
        requestsThisMinute = 0;
    }

    public synchronized void resetMonth() {
        tokensUsedThisMonth = 0;
    }

    public synchronized int getRequestsThisMinute() {
        return requestsThisMinute;
    }

    public synchronized long getTokensUsedThisMonth() {
        return tokensUsedThisMonth;
    }

    public synchronized void setTokensUsedThisMonth(long tokens) {
        this.tokensUsedThisMonth = tokens;
    }

    public synchronized DailyUsage getDay(LocalDate day) {
        DailyUsage d = history.get(day);
        return d == null ? new DailyUsage(day, 0, 0) : new DailyUsage(day, d.getRequests(), d.getTokens());
    }

    public synchronized void putDay(DailyUsage usage) {
        history.put(usage.getDate(), usage);
    }
}
