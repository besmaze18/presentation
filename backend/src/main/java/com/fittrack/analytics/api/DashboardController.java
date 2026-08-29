package com.fittrack.analytics.api;

import com.fittrack.analytics.dto.DashboardResponse;
import com.fittrack.analytics.service.DashboardService;
import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "The Today view, calculated deterministically")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Nutrition, energy, weight, wearable and workout summary for one day")
    public DashboardResponse dashboard(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dashboardService.today(currentUser.getId(), date);
    }
}
