package com.fittrack.user.dto;

import com.fittrack.user.domain.ActivityLevel;
import com.fittrack.user.domain.Sex;
import com.fittrack.user.domain.UnitSystem;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateUserSettingsRequest(
        @Size(max = 64) String timeZone,
        UnitSystem unitSystem,
        Sex sex,
        @Past LocalDate birthDate,
        @DecimalMin("50.0") @DecimalMax("260.0") BigDecimal heightCm,
        ActivityLevel activityLevel) {}
