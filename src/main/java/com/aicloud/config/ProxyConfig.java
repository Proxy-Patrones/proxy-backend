package com.aicloud.config;

import com.aicloud.proxy.QuotaProxyService;
import com.aicloud.proxy.RateLimitProxyService;
import com.aicloud.repository.UserRepository;
import com.aicloud.service.AIGenerationService;
import com.aicloud.service.AppClock;
import com.aicloud.service.DemoDataLoader;
import com.aicloud.service.MockAIGenerationService;
import com.aicloud.service.QuotaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Aqui se arma la cadena del patron Proxy:
 *
 *   Controller -> RateLimitProxyService -> QuotaProxyService -> MockAIGenerationService
 *
 * Solo existe UN bean de tipo AIGenerationService (el primer proxy),
 * asi nadie puede saltarse los controles y llamar directo al servicio real.
 */
@Configuration
public class ProxyConfig {

    @Bean
    public AppClock appClock(@Value("${app.zone}") String zone) {
        return new AppClock(ZoneId.of(zone));
    }

    @Bean
    public UserRepository userRepository(AppClock clock) {
        UserRepository repository = new UserRepository();
        new DemoDataLoader(repository, clock).load();
        return repository;
    }

    @Bean
    public QuotaService quotaService(UserRepository userRepository, AppClock clock) {
        return new QuotaService(userRepository, clock);
    }

    @Bean
    public AIGenerationService aiGenerationService(UserRepository userRepository, AppClock clock) {
        AIGenerationService realService = new MockAIGenerationService();
        AIGenerationService quotaProxy = new QuotaProxyService(realService, userRepository, clock);
        return new RateLimitProxyService(quotaProxy, userRepository, clock);
    }
}
