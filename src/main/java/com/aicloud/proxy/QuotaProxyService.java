package com.aicloud.proxy;

import com.aicloud.exception.QuotaExceededException;
import com.aicloud.exception.UserNotFoundException;
import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.model.UserUsage;
import com.aicloud.repository.UserRepository;
import com.aicloud.service.AIGenerationService;
import com.aicloud.service.AppClock;
import com.aicloud.service.TokenEstimator;

/**
 * PROXY 2 de la cadena: controla la cuota mensual de tokens.
 * 1. Reserva los tokens estimados del prompt (si no alcanzan -> 402).
 * 2. Llama al servicio real.
 * 3. Descuenta los tokens reales y registra el consumo del dia.
 */
public class QuotaProxyService implements AIGenerationService {

    private final AIGenerationService realService;
    private final UserRepository userRepository;
    private final AppClock clock;

    public QuotaProxyService(AIGenerationService realService, UserRepository userRepository, AppClock clock) {
        this.realService = realService;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        Plan plan = user.getPlan();
        UserUsage usage = userRepository.usageOf(user.getId());

        long estimated = TokenEstimator.estimate(request.getPrompt());
        if (!usage.tryReserveTokens(estimated, plan.getMonthlyTokens())) {
            throw new QuotaExceededException(
                    "Agotaste tu cuota mensual de " + plan.getMonthlyTokens() + " tokens del plan " + plan
                            + ". Mejora tu plan para seguir usando el servicio.");
        }

        try {
            GenerationResponse response = realService.generate(request);
            usage.confirmTokens(estimated, response.getTotalTokens(), clock.today());
            return response;
        } catch (RuntimeException e) {
            usage.releaseTokens(estimated); // si el servicio real falla, no se cobra
            throw e;
        }
    }
}
