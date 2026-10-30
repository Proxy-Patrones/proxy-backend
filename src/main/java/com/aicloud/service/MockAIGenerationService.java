package com.aicloud.service;

import com.aicloud.model.GenerationRequest;
import com.aicloud.model.GenerationResponse;

import java.util.List;
import java.util.Random;

/**
 * RealSubject del patron Proxy: el "servicio de IA" real.
 * No sabe nada de planes, cuotas ni limites: solo genera texto.
 */
public class MockAIGenerationService implements AIGenerationService {

    private static final List<String> RESPONSES = List.of(
            "El patron Proxy coloca un intermediario entre el cliente y el objeto real. "
                    + "Ese intermediario implementa la misma interfaz, por eso el cliente no nota la diferencia. "
                    + "Antes de delegar la llamada puede validar permisos, contar solicitudes, usar cache o registrar el consumo.",
            "Una buena forma de entenderlo es pensar en el vigilante de una piscina: la piscina es el objeto real "
                    + "y el vigilante es el proxy. Nadie entra sin pasar por el, y el decide segun reglas claras quien puede pasar y quien no.",
            "En una API de inteligencia artificial el rate limiting protege la infraestructura: limita cuantas solicitudes "
                    + "puede hacer cada usuario por minuto. La cuota mensual, en cambio, controla cuantos tokens puede consumir en total segun su plan.",
            "Lazy loading significa crear o cargar un recurso solo cuando de verdad se necesita. Un proxy virtual puede "
                    + "retrasar la creacion de un objeto costoso hasta la primera vez que el cliente lo use, ahorrando memoria y tiempo de arranque.",
            "Redis es un almacenamiento en memoria muy rapido que se usa como cache. Un proxy de cache revisa primero si la "
                    + "respuesta ya esta guardada; si esta, la devuelve de inmediato y evita llamar otra vez al servicio real.",
            "Para disenar bien un sistema conviene empezar por los diagramas: identificar el cliente, la interfaz comun, "
                    + "el objeto real y los proxies. Con el diseno claro, el codigo sale mas ordenado y es mas facil de mantener.",
            "Los errores HTTP 429 y 402 comunican cosas distintas. El 429 Too Many Requests indica que se debe esperar "
                    + "el tiempo del header Retry-After. El 402 Payment Required indica que la cuota se agoto y hace falta mejorar el plan."
    );

    private final Random random = new Random();
    private final long delayMs;

    public MockAIGenerationService() {
        this(1200);
    }

    public MockAIGenerationService(long delayMs) {
        this.delayMs = delayMs;
    }

    @Override
    public GenerationResponse generate(GenerationRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(delayMs); // simula la latencia del modelo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String text = RESPONSES.get(random.nextInt(RESPONSES.size()));
        int promptTokens = TokenEstimator.estimate(request.getPrompt());
        int completionTokens = TokenEstimator.estimate(text);
        return new GenerationResponse(text, promptTokens, completionTokens, System.currentTimeMillis() - start);
    }
}
