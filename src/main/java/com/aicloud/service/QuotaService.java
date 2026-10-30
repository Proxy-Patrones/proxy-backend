package com.aicloud.service;

import com.aicloud.dto.QuotaStatus;
import com.aicloud.dto.UpgradeRequest;
import com.aicloud.exception.UserNotFoundException;
import com.aicloud.model.DailyUsage;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.model.UserUsage;
import com.aicloud.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consulta de cuota, historial, upgrade de plan y resets programados. */
public class QuotaService {

    private final UserRepository userRepository;
    private final AppClock clock;

    public QuotaService(UserRepository userRepository, AppClock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public QuotaStatus status(String userId) {
        User user = getUser(userId);
        Plan plan = user.getPlan();
        UserUsage usage = userRepository.usageOf(user.getId());

        long used = usage.getTokensUsedThisMonth();
        long limit = plan.getMonthlyTokens();
        long remaining = limit < 0 ? -1 : Math.max(0, limit - used);
        int requests = usage.getRequestsThisMinute();
        int rpm = plan.getRequestsPerMinute();

        return new QuotaStatus(
                user.getId(), plan, used, remaining, limit, clock.nextMonthlyReset(),
                requests, rpm, clock.secondsUntilNextMinute(),
                rpm >= 0 && requests >= rpm,
                limit >= 0 && remaining <= 0);
    }

    /** Ultimos 7 dias (incluido hoy), del mas antiguo al mas reciente. Los dias sin uso salen en 0. */
    public List<DailyUsage> history(String userId) {
        User user = getUser(userId);
        UserUsage usage = userRepository.usageOf(user.getId());
        LocalDate today = clock.today();
        List<DailyUsage> days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            days.add(usage.getDay(today.minusDays(i)));
        }
        return days;
    }

    /** Upgrade con pago simulado. Solo permite subir de plan (FREE -> PRO -> ENTERPRISE). */
    public QuotaStatus upgrade(String userId, UpgradeRequest request) {
        User user = getUser(userId);
        Plan target = parsePlan(request == null ? null : request.plan());

        if (!user.getPlan().canUpgradeTo(target)) {
            throw new IllegalArgumentException("No puedes pasar de " + user.getPlan() + " a " + target + ".");
        }
        simulatePayment(request);
        user.setPlan(target);
        return status(userId);
    }

    public void resetRateLimits() {
        userRepository.allUsages().forEach(UserUsage::resetMinute);
    }

    public void resetMonthlyQuotas() {
        userRepository.allUsages().forEach(UserUsage::resetMonth);
    }

    private void simulatePayment(UpgradeRequest request) {
        String card = request.cardNumber() == null ? "" : request.cardNumber().replaceAll("\\s", "");
        if (request.cardHolder() == null || request.cardHolder().isBlank()) {
            throw new IllegalArgumentException("Escribe el nombre del titular de la tarjeta.");
        }
        if (!card.matches("\\d{16}")) {
            throw new IllegalArgumentException("El número de tarjeta debe tener 16 dígitos.");
        }
        if (request.expiry() == null || !request.expiry().matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("La fecha de vencimiento debe tener el formato MM/AA.");
        }
        if (request.cvv() == null || !request.cvv().matches("\\d{3,4}")) {
            throw new IllegalArgumentException("El CVV debe tener 3 o 4 dígitos.");
        }
        if (card.endsWith("0000")) { // tarjeta de prueba para simular un rechazo
            throw new IllegalArgumentException("Pago rechazado por el banco (simulación). Prueba con otra tarjeta.");
        }
    }

    private Plan parsePlan(String value) {
        try {
            return Plan.valueOf(value == null ? "" : value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Plan no válido. Usa PRO o ENTERPRISE.");
        }
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
