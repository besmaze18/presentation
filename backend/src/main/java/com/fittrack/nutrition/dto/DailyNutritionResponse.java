package com.fittrack.nutrition.dto;

import java.time.LocalDate;
import java.util.List;

/** Everything the nutrition screen needs for one day in a single round trip. */
public record DailyNutritionResponse(
        LocalDate date, MacrosDto totals, long entryCount, List<FoodEntryResponse> entries) {}
