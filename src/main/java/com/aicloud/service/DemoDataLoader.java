package com.aicloud.service;

import com.aicloud.model.DailyUsage;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.model.UserUsage;
import com.aicloud.repository.UserRepository;

import java.time.LocalDate;

/**
 * Crea tres usuarios de demostracion con historial de los ultimos dias.
 * "demo-free" arranca casi sin cuota para poder mostrar el modal de upgrade en la sustentacion.
 */
public class DemoDataLoader {

    private final UserRepository userRepository;
    private final AppClock clock;

    public DemoDataLoader(UserRepository userRepository, AppClock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public void load() {
        create("demo-free", Plan.FREE, 49_850, new int[]{4, 9, 6, 12, 3, 8});
        create("demo-pro", Plan.PRO, 182_350, new int[]{40, 55, 31, 70, 62, 48});
        create("demo-enterprise", Plan.ENTERPRISE, 1_240_800, new int[]{210, 340, 180, 400, 290, 375});
    }

    private void create(String id, Plan plan, long tokensUsed, int[] requestsPerDay) {
        User user = userRepository.save(new User(id, plan));
        UserUsage usage = userRepository.usageOf(user.getId());
        usage.setTokensUsedThisMonth(tokensUsed);

        LocalDate today = clock.today();
        for (int i = 0; i < requestsPerDay.length; i++) {
            int requests = requestsPerDay[i];
            LocalDate day = today.minusDays(requestsPerDay.length - i); // de hace 6 dias hasta ayer
            usage.putDay(new DailyUsage(day, requests, requests * 105L));
        }
    }
}
