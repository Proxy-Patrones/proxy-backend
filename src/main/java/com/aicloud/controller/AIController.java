package com.aicloud.controller;

import com.aicloud.dto.PromptRequest;
import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;
import com.aicloud.service.AIGenerationService;
import com.aicloud.service.QuotaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cliente del patron Proxy: recibe un AIGenerationService
 * y no sabe que en realidad es la cadena RateLimit -> Quota -> Mock.
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final int MAX_PROMPT_LENGTH = 4000;

    private final AIGenerationService aiService;
    private final QuotaService quotaService;

    public AIController(AIGenerationService aiService, QuotaService quotaService) {
        this.aiService = aiService;
        this.quotaService = quotaService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestHeader("X-User-Id") String userId,
                                        @RequestBody PromptRequest body) {
        String prompt = body == null ? null : body.prompt();
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("El prompt no puede estar vacío.");
        }
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            throw new IllegalArgumentException("El prompt supera los " + MAX_PROMPT_LENGTH + " caracteres.");
        }

        GenerationResponse response = aiService.generate(new GenerationRequest(userId, prompt.trim()));
        return Map.of(
                "response", response,
                "quota", quotaService.status(userId));
    }
}
