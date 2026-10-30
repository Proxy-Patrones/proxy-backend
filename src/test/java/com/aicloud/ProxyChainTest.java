package com.aicloud;

import com.aicloud.exception.QuotaExceededException;
import com.aicloud.exception.RateLimitExceededException;
import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.proxy.QuotaProxyService;
import com.aicloud.proxy.RateLimitProxyService;
import com.aicloud.repository.UserRepository;
import com.aicloud.service.AIGenerationService;
import com.aicloud.service.AppClock;
import com.aicloud.service.MockAIGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyChainTest {

    private UserRepository repository;
    private AIGenerationService chain;

    @BeforeEach
    void setUp() {
        AppClock clock = new AppClock(ZoneId.of("America/Bogota"));
        repository = new UserRepository();
        AIGenerationService real = new MockAIGenerationService(0); // sin espera en pruebas
        chain = new RateLimitProxyService(new QuotaProxyService(real, repository, clock), repository, clock);
    }

    @Test
    void freeUserIsBlockedAfterTenRequestsPerMinute() {
        repository.save(new User("ana", Plan.FREE));
        for (int i = 0; i < 10; i++) {
            chain.generate(new GenerationRequest("ana", "hola"));
        }
        RateLimitExceededException e = assertThrows(RateLimitExceededException.class,
                () -> chain.generate(new GenerationRequest("ana", "hola")));
        assertTrue(e.getRetryAfterSeconds() > 0 && e.getRetryAfterSeconds() <= 60);
    }

    @Test
    void quotaIsDiscountedAndExhausted() {
        repository.save(new User("luis", Plan.FREE));
        GenerationResponse r = chain.generate(new GenerationRequest("luis", "hola"));
        assertEquals(r.getTotalTokens(), repository.usageOf("luis").getTokensUsedThisMonth());

        repository.usageOf("luis").setTokensUsedThisMonth(50_000);
        repository.usageOf("luis").resetMinute();
        assertThrows(QuotaExceededException.class, () -> chain.generate(new GenerationRequest("luis", "hola")));
    }

    @Test
    void enterpriseHasNoLimits() {
        repository.save(new User("corp", Plan.ENTERPRISE));
        for (int i = 0; i < 100; i++) {
            chain.generate(new GenerationRequest("corp", "hola"));
        }
        assertEquals(100, repository.usageOf("corp").getRequestsThisMinute());
    }
}
