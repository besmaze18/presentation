package com.fittrack.training.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTrainingTotals(
        LocalDate date,
        long sessionCount,
        long totalDurationMinutes,
        BigDecimal totalCaloriesKcal,
        BigDecimal maxStrain) {}
