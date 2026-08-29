package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.FoodItem;
import com.fittrack.nutrition.domain.Macros;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record FoodItemDto(
        @NotBlank @Size(max = 200) String name,
        @DecimalMin("0.0") BigDecimal quantity,
        @Size(max = 32) String unit,
        @NotNull @Valid MacrosDto macros) {

    public FoodItem toEntity() {
        return new FoodItem(name.trim(), quantity, unit, macros.toMacros());
    }

    public static FoodItemDto from(FoodItem item) {
        return new FoodItemDto(
                item.getName(), item.getQuantity(), item.getUnit(), MacrosDto.from(item.getMacros()));
    }

    public static FoodItemDto of(String name, BigDecimal quantity, String unit, Macros macros) {
        return new FoodItemDto(name, quantity, unit, MacrosDto.from(macros));
    }
}
