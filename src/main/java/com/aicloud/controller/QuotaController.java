package com.aicloud.controller;

import com.aicloud.dto.QuotaStatus;
import com.aicloud.dto.UpgradeRequest;
import com.aicloud.model.DailyUsage;
import com.aicloud.service.QuotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuotaService quotaService;

    public QuotaController(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping("/status")
    public QuotaStatus status(@RequestHeader("X-User-Id") String userId) {
        return quotaService.status(userId);
    }

    @GetMapping("/history")
    public List<DailyUsage> history(@RequestHeader("X-User-Id") String userId) {
        return quotaService.history(userId);
    }

    @PostMapping("/upgrade")
    public QuotaStatus upgrade(@RequestHeader("X-User-Id") String userId,
                               @RequestBody UpgradeRequest request) {
        return quotaService.upgrade(userId, request);
    }
}
