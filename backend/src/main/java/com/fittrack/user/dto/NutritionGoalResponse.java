package com.fittrack.user.dto;

import com.fittrack.user.domain.NutritionGoal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record NutritionGoalResponse(
        UUID id,
        LocalDate effectiveFrom,
        Integer calorieTarget,
        BigDecimal proteinTargetG,
        BigDecimal carbsTargetG,
        BigDecimal fatTargetG,
        BigDecimal fiberTargetG,
        BigDecimal targetBodyWeightKg) {

    public static NutritionGoalResponse from(NutritionGoal goal) {
        return new NutritionGoalResponse(
                goal.getId(),
                goal.getEffectiveFrom(),
                goal.getCalorieTarget(),
                goal.getProteinTargetG(),
                goal.getCarbsTargetG(),
                goal.getFatTargetG(),
                goal.getFiberTargetG(),
                goal.getTargetBodyWeightKg());
    }
}
