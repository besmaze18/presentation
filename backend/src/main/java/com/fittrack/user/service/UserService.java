package com.fittrack.user.service;

import com.fittrack.common.exception.BadRequestException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.user.domain.NutritionGoal;
import com.fittrack.user.domain.NutritionGoalRepository;
import com.fittrack.user.domain.User;
import com.fittrack.user.domain.UserRepository;
import com.fittrack.user.domain.UserSettings;
import com.fittrack.user.domain.UserSettingsRepository;
import com.fittrack.user.dto.UpdateProfileRequest;
import com.fittrack.user.dto.UpdateUserSettingsRequest;
import com.fittrack.user.dto.UpsertNutritionGoalRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final Clock clock;

    public UserService(
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            NutritionGoalRepository nutritionGoalRepository,
            Clock clock) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.nutritionGoalRepository = nutritionGoalRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> NotFoundException.of("User", userId));
    }

    @Transactional(readOnly = true)
    public UserSettings requireSettings(UUID userId) {
        return userSettingsRepository
                .findByUserId(userId)
                .orElseThrow(() -> NotFoundException.of("Settings for user", userId));
    }

    @Transactional(readOnly = true)
    public ZoneId zoneOf(UUID userId) {
        return ZoneId.of(requireSettings(userId).getTimeZone());
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        user.setDisplayName(request.displayName().trim());
        return userRepository.save(user);
    }

    @Transactional
    public UserSettings updateSettings(UUID userId, UpdateUserSettingsRequest request) {
        UserSettings settings = requireSettings(userId);
        if (request.timeZone() != null && !request.timeZone().isBlank()) {
            settings.setTimeZone(parseZone(request.timeZone()));
        }
        if (request.unitSystem() != null) {
            settings.setUnitSystem(request.unitSystem());
        }
        if (request.sex() != null) {
            settings.setSex(request.sex());
        }
        if (request.birthDate() != null) {
            settings.setBirthDate(request.birthDate());
        }
        if (request.heightCm() != null) {
            settings.setHeightCm(request.heightCm());
        }
        if (request.activityLevel() != null) {
            settings.setActivityLevel(request.activityLevel());
        }
        return userSettingsRepository.save(settings);
    }

    /** The goal in effect on a given day - the basis for every target shown on the dashboard. */
    @Transactional(readOnly = true)
    public NutritionGoal goalInEffect(UUID userId, LocalDate date) {
        return nutritionGoalRepository
                .findFirstByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(userId, date)
                .orElseGet(() -> nutritionGoalRepository
                        .findByUserIdOrderByEffectiveFromDesc(userId, Limit.of(1))
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("No nutrition goal configured")));
    }

    @Transactional(readOnly = true)
    public List<NutritionGoal> goalHistory(UUID userId, int limit) {
        return nutritionGoalRepository.findByUserIdOrderByEffectiveFromDesc(
                userId, Limit.of(Math.clamp(limit, 1, 100)));
    }

    @Transactional
    public NutritionGoal upsertGoal(UUID userId, UpsertNutritionGoalRequest request) {
        User user = requireUser(userId);
        LocalDate effectiveFrom = request.effectiveFrom() != null
                ? request.effectiveFrom()
                : LocalDate.now(clock.withZone(zoneOf(userId)));

        NutritionGoal goal = nutritionGoalRepository
                .findByUserIdAndEffectiveFrom(userId, effectiveFrom)
                .orElseGet(() -> new NutritionGoal(user, effectiveFrom));

        goal.setCalorieTarget(request.calorieTarget());
        goal.setProteinTargetG(request.proteinTargetG());
        goal.setCarbsTargetG(request.carbsTargetG());
        goal.setFatTargetG(request.fatTargetG());
        goal.setFiberTargetG(request.fiberTargetG());
        goal.setTargetBodyWeightKg(request.targetBodyWeightKg());
        return nutritionGoalRepository.save(goal);
    }

    private String parseZone(String zone) {
        try {
            return ZoneId.of(zone.trim()).getId();
        } catch (RuntimeException ex) {
            throw new BadRequestException("Unknown time zone: " + zone);
        }
    }
}
