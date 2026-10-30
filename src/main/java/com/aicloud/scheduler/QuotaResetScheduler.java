package com.aicloud.scheduler;

import com.aicloud.service.QuotaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Tareas programadas que reinician los contadores. */
@Component
public class QuotaResetScheduler {

    private final QuotaService quotaService;

    public QuotaResetScheduler(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    /** Segundo 0 de cada minuto: reinicia el rate limit. */
    @Scheduled(cron = "0 * * * * *", zone = "${app.zone}")
    public void resetRateLimits() {
        quotaService.resetRateLimits();
    }

    /** 00:00 del dia 1 de cada mes: reinicia la cuota mensual de tokens. */
    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.zone}")
    public void resetMonthlyQuotas() {
        quotaService.resetMonthlyQuotas();
    }
}
