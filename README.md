# Plataforma de consumo de IA con Proxy de rate limiting y cuotas — Backend

Taller individual de **Patrones de Software** — patrón estructural **Proxy**.
Backend en **Java 21 + Spring Boot 3**. Los datos se guardan en memoria.

## ¿Dónde está el patrón Proxy?

```
Frontend ──► AIController ──► RateLimitProxyService ──► QuotaProxyService ──► MockAIGenerationService
 (cliente)                     (proxy 1: req/minuto)     (proxy 2: tokens/mes)   (objeto real)
```

| Rol en el patrón | Clase | Responsabilidad |
| --- | --- | --- |
| Subject (interfaz común) | `service/AIGenerationService` | Define `generate(GenerationRequest): GenerationResponse` |
| Real Subject | `service/MockAIGenerationService` | Simula la IA: `Thread.sleep(1200)` y texto aleatorio predefinido |
| Proxy 1 | `proxy/RateLimitProxyService` | Verifica las requests por minuto del plan. Si se superan → **429 Too Many Requests** + header `Retry-After` |
| Proxy 2 | `proxy/QuotaProxyService` | Verifica la cuota mensual de tokens y descuenta los tokens del request. Si se agota → **402 Payment Required** |
| Cliente | `controller/AIController` | Solo conoce la interfaz `AIGenerationService` |
| Armado de la cadena | `config/ProxyConfig` | Encadena RateLimit → Quota → Mock. Es el único bean de IA, así nadie puede saltarse los proxies |

Los tres implementan la **misma interfaz**, por eso el controller no sabe si habla con un proxy o con el servicio real.

## Planes

| Plan | Requests por minuto | Tokens por mes |
| --- | --- | --- |
| FREE | 10 | 50.000 |
| PRO | 60 | 500.000 |
| ENTERPRISE | ilimitado | ilimitado |

Tokens estimados: 1 token ≈ 4 caracteres (`service/TokenEstimator`).

## Endpoints

Todas las rutas de `/api/ai` y `/api/quota` necesitan el header `X-User-Id`.

| Método | Ruta | Qué hace |
| --- | --- | --- |
| POST | `/api/ai/generate` | `{ "prompt": "..." }` — pasa por la cadena completa de proxies |
| GET | `/api/quota/status` | Tokens usados, restantes, `resetDate`, plan actual y requests del minuto |
| GET | `/api/quota/history` | Uso diario de los últimos 7 días |
| POST | `/api/quota/upgrade` | `{ "plan": "PRO", "cardHolder", "cardNumber", "expiry", "cvv" }` — pago simulado |
| POST | `/api/users/login` | `{ "username": "ana" }` — si no existe se crea con plan FREE |
| GET | `/api/users/plans` | Lista de planes |
| GET | `/api/health` | Estado del servicio |

Respuestas de error: `429` (con `Retry-After` y `retryAfterSeconds`), `402`, `404` y `400`, todas con `{ status, error, message }`.

## Tareas programadas (`scheduler/QuotaResetScheduler`)

- `0 * * * * *` → reinicia el rate limit cada minuto.
- `0 0 0 1 * *` → reinicia la cuota mensual el día 1 de cada mes.

## Usuarios de demostración

| Usuario | Plan | Para qué sirve |
| --- | --- | --- |
| `demo-free` | FREE | Le quedan ~150 tokens: con 3 o 4 mensajes se ve el 402 y el modal de upgrade |
| `demo-pro` | PRO | Uso intermedio con historial |
| `demo-enterprise` | ENTERPRISE | Sin límites |

Tarjeta de prueba aprobada: `4242 4242 4242 4242`. Cualquier tarjeta que termine en `0000` es rechazada (simulación).

## Ejecutar en local

```bash
mvn spring-boot:run
# o con Docker
docker build -t proxy-ai-backend .
docker run -p 8080:8080 proxy-ai-backend
```

Pruebas: `mvn test` (`ProxyChainTest` prueba el 429, el 402 y el plan ilimitado).

## Despliegue en Railway

1. **New Project → Deploy from GitHub repo** → este repositorio (Railway detecta el `Dockerfile`).
2. Variables: `FRONTEND_URL` = URL de Vercel (sin `/` al final). Opcional: `APP_ZONE` (por defecto `America/Bogota`).
3. **Settings → Networking → Generate Domain**.
4. Probar `https://<dominio>/api/health`.
