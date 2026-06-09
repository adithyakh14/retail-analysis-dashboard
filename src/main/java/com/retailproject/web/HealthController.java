package com.retailproject.web;

import com.retailproject.DashboardDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Platform", description = "Deployment and readiness endpoints")
public class HealthController {
    private final DashboardDataService dashboardDataService;

    public HealthController(DashboardDataService dashboardDataService) {
        this.dashboardDataService = dashboardDataService;
    }

    @GetMapping("/api/health")
    @Operation(summary = "Deployment health check", description = "Returns a lightweight readiness payload for hosting platforms and manual checks.")
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("application", "Retail Analysis Dashboard");
        response.put("timestamp", Instant.now().toString());
        response.put("datasetRecords", dashboardDataService.recordCount());
        response.put("refreshedAt", dashboardDataService.refreshedAt());
        response.put("authentication", "form-login-for-browser,bearer-token-for-api");
        return response;
    }
}
