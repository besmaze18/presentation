package com.fittrack.analytics.dto;

import com.fittrack.training.dto.TrainingSessionResponse;
import com.fittrack.user.dto.WeightTrendResponse;
import java.time.LocalDate;
import java.util.List;

/** Everything the Today screen renders, assembled server-side in one round trip. */
public record DashboardResponse(
        LocalDate date,
        String timeZone,
        NutritionSummary nutrition,
        EnergySummary energy,
        WeightTrendResponse weight,
        WearableDaySnapshot wearable,
        boolean wearableConnected,
        List<TrainingSessionResponse> workoutsToday,
        List<InsightDto> insights) {

    public record NutritionSummary(
            MacroProgress calories,
            MacroProgress protein,
            MacroProgress carbs,
            MacroProgress fat,
            MacroProgress fiber,
            long entryCount) {}
}
