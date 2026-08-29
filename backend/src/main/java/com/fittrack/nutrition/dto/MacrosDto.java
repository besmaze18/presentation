package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.Macros;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MacrosDto(
        @NotNull @DecimalMin("0.0") @DecimalMax("30000.0") BigDecimal calories,
        @NotNull @DecimalMin("0.0") @DecimalMax("2000.0") BigDecimal proteinG,
        @NotNull @DecimalMin("0.0") @DecimalMax("3000.0") BigDecimal carbsG,
        @NotNull @DecimalMin("0.0") @DecimalMax("2000.0") BigDecimal fatG,
        @NotNull @DecimalMin("0.0") @DecimalMax("500.0") BigDecimal fiberG) {

    public Macros toMacros() {
        return new Macros(calories, proteinG, carbsG, fatG, fiberG);
    }

    public static MacrosDto from(Macros macros) {
        return new MacrosDto(
                macros.getCalories(),
                macros.getProteinG(),
                macros.getCarbsG(),
                macros.getFatG(),
                macros.getFiberG());
    }
}
