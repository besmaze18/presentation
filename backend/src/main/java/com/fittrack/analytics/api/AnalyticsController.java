package com.fittrack.analytics.api;

import com.fittrack.analytics.dto.AnalyticsSeriesResponse;
import com.fittrack.analytics.service.AnalyticsService;
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
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Aggregated history for the daily, weekly and monthly views")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/series")
    @Operation(
            summary = "Day/week/month series across nutrition, weight, training and wearable metrics",
            description =
                    "Aggregation is performed in the database. Granularity defaults to the most "
                            + "readable bucket size for the requested range.")
    public AnalyticsSeriesResponse series(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) AnalyticsService.Granularity granularity) {
        AnalyticsService.Granularity resolved =
                granularity != null ? granularity : AnalyticsService.defaultGranularityFor(from, to);
        return analyticsService.series(currentUser.getId(), from, to, resolved);
    }
}
