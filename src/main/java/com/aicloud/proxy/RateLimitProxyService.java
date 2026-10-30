package com.aicloud.proxy;

import com.aicloud.exception.RateLimitExceededException;
import com.aicloud.exception.UserNotFoundException;
import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.repository.UserRepository;
import com.aicloud.service.AIGenerationService;
import com.aicloud.service.AppClock;

/**
 * PROXY 1 de la cadena: controla las requests por minuto.
 * Si el usuario supera el limite de su plan, corta la cadena con 429 + Retry-After.
 * Si no, delega en el siguiente eslabon (QuotaProxyService).
 */
public class RateLimitProxyService implements AIGenerationService {

    private final AIGenerationService next;
    private final UserRepository userRepository;
    private final AppClock clock;

    public RateLimitProxyService(AIGenerationService next, UserRepository userRepository, AppClock clock) {
        this.next = next;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        Plan plan = user.getPlan();

        boolean allowed = userRepository.usageOf(user.getId())
                .tryAcquireRequest(plan.getRequestsPerMinute());

        if (!allowed) {
            long retryAfter = clock.secondsUntilNextMinute();
            throw new RateLimitExceededException(
                    "Superaste el límite de " + plan.getRequestsPerMinute() + " requests por minuto del plan "
                            + plan + ". Intenta de nuevo en " + retryAfter + " s.",
                    retryAfter);
        }
        return next.generate(request);
    }
}
