package com.aicloud.dto;

import com.aicloud.model.Plan;

import java.time.LocalDateTime;

/** Respuesta de GET /api/quota/status. Un limite de -1 significa ilimitado. */
public record QuotaStatus(
        String userId,
        Plan plan,
        long tokensUsed,
        long tokensRemaining,
        long tokenLimit,
        LocalDateTime resetDate,
        int requestsThisMinute,
        int requestsPerMinuteLimit,
        long secondsUntilRateLimitReset,
        boolean rateLimited,
        boolean quotaExhausted
) {
}
