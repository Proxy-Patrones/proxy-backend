package com.aicloud.service;

import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;

/**
 * Subject del patron Proxy.
 * La implementan el servicio real (MockAIGenerationService) y los dos proxies,
 * por eso el controller no sabe si habla con un proxy o con el servicio real.
 */
public interface AIGenerationService {

    GenerationResponse generate(GenerationRequest request);
}
