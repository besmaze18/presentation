package com.fittrack.user.dto;

import com.fittrack.user.domain.UserSettings;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UserSettingsResponse(
        String timeZone,
        String unitSystem,
        String sex,
        LocalDate birthDate,
        BigDecimal heightCm,
        String activityLevel) {

    public static UserSettingsResponse from(UserSettings settings) {
        return new UserSettingsResponse(
                settings.getTimeZone(),
                settings.getUnitSystem().name(),
                settings.getSex().name(),
                settings.getBirthDate(),
                settings.getHeightCm(),
                settings.getActivityLevel().name());
    }
}
