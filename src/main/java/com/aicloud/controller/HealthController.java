package com.aicloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/", "/api/health"})
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "Plataforma de consumo de IA con Proxy de rate limiting y cuotas",
                "chain", "RateLimitProxyService -> QuotaProxyService -> MockAIGenerationService");
    }
}
