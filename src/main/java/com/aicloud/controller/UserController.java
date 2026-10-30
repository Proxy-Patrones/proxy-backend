package com.aicloud.controller;

import com.aicloud.dto.LoginRequest;
import com.aicloud.model.Plan;
import com.aicloud.model.User;
import com.aicloud.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Ingreso simple por nombre de usuario: si no existe se crea con plan FREE. */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String username = request == null || request.username() == null ? "" : request.username().trim();
        if (!username.matches("[A-Za-z0-9_-]{3,20}")) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 20 caracteres (letras, números, - o _).");
        }
        User user = userRepository.findOrCreate(username);
        return Map.of("userId", user.getId(), "plan", user.getPlan());
    }

    /** Lista de planes para pintar las tarjetas del frontend. */
    @GetMapping("/plans")
    public List<Map<String, Object>> plans() {
        return Arrays.stream(Plan.values())
                .map(p -> Map.<String, Object>of(
                        "name", p.name(),
                        "requestsPerMinute", p.getRequestsPerMinute(),
                        "monthlyTokens", p.getMonthlyTokens(),
                        "priceUsd", p.getPriceUsd()))
                .toList();
    }
}
