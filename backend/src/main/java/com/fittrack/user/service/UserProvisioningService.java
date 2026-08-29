package com.fittrack.user.service;

import com.fittrack.user.domain.NutritionGoal;
import com.fittrack.user.domain.NutritionGoalRepository;
import com.fittrack.user.domain.User;
import com.fittrack.user.domain.UserSettings;
import com.fittrack.user.domain.UserSettingsRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the rows every user needs to be immediately usable: settings and a starting set of
 * nutrition targets. Without a starting goal the dashboard would have nothing to compare against.
 */
@Service
public class UserProvisioningService {

    private static final int DEFAULT_CALORIES = 2400;
    private static final BigDecimal DEFAULT_PROTEIN_G = new BigDecimal("160.0");
    private static final BigDecimal DEFAULT_CARBS_G = new BigDecimal("260.0");
    private static final BigDecimal DEFAULT_FAT_G = new BigDecimal("80.0");
    private static final BigDecimal DEFAULT_FIBER_G = new BigDecimal("30.0");

    private final UserSettingsRepository userSettingsRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final Clock clock;

    public UserProvisioningService(
            UserSettingsRepository userSettingsRepository,
            NutritionGoalRepository nutritionGoalRepository,
            Clock clock) {
        this.userSettingsRepository = userSettingsRepository;
        this.nutritionGoalRepository = nutritionGoalRepository;
        this.clock = clock;
    }

    @Transactional
    public void initialiseNewUser(User user, String requestedTimeZone) {
        UserSettings settings = new UserSettings(user);
        settings.setTimeZone(normaliseZone(requestedTimeZone));
        userSettingsRepository.save(settings);

        LocalDate today = LocalDate.now(clock.withZone(ZoneId.of(settings.getTimeZone())));
        NutritionGoal goal = new NutritionGoal(user, today);
        goal.setCalorieTarget(DEFAULT_CALORIES);
        goal.setProteinTargetG(DEFAULT_PROTEIN_G);
        goal.setCarbsTargetG(DEFAULT_CARBS_G);
        goal.setFatTargetG(DEFAULT_FAT_G);
        goal.setFiberTargetG(DEFAULT_FIBER_G);
        nutritionGoalRepository.save(goal);
    }

    private String normaliseZone(String requestedTimeZone) {
        if (requestedTimeZone == null || requestedTimeZone.isBlank()) {
            return "UTC";
        }
        try {
            return ZoneId.of(requestedTimeZone.trim()).getId();
        } catch (RuntimeException ex) {
            return "UTC";
        }
    }
}
