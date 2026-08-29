package com.fittrack.common.seed;

import com.fittrack.nutrition.domain.FoodEntry;
import com.fittrack.nutrition.domain.FoodEntryRepository;
import com.fittrack.nutrition.domain.FoodEntrySource;
import com.fittrack.nutrition.domain.Macros;
import com.fittrack.nutrition.domain.MealType;
import com.fittrack.nutrition.domain.SavedFood;
import com.fittrack.nutrition.domain.SavedFoodRepository;
import com.fittrack.training.domain.TrainingCategory;
import com.fittrack.training.domain.TrainingSession;
import com.fittrack.training.domain.TrainingSessionRepository;
import com.fittrack.training.domain.TrainingSource;
import com.fittrack.user.domain.ActivityLevel;
import com.fittrack.user.domain.BodyMeasurement;
import com.fittrack.user.domain.BodyMeasurementRepository;
import com.fittrack.user.domain.MeasurementSource;
import com.fittrack.user.domain.NutritionGoal;
import com.fittrack.user.domain.NutritionGoalRepository;
import com.fittrack.user.domain.Sex;
import com.fittrack.user.domain.User;
import com.fittrack.user.domain.UserRepository;
import com.fittrack.user.domain.UserSettings;
import com.fittrack.user.domain.UserSettingsRepository;
import com.fittrack.user.service.UserProvisioningService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optional development seed data.
 *
 * <p>Off unless {@code SEED_ENABLED=true}, and refuses to run without an explicitly configured
 * password, so no instance ever ships with a known account. It is also a no-op once the demo user
 * exists, so restarting the stack does not pile up duplicate history.
 */
@Component
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    /** Fixed so a seeded database looks the same every time it is created. */
    private static final long RANDOM_SEED = 20260310L;

    private final SeedProperties properties;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final NutritionGoalRepository nutritionGoalRepository;
    private final FoodEntryRepository foodEntryRepository;
    private final SavedFoodRepository savedFoodRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;
    private final UserProvisioningService userProvisioningService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DevDataSeeder(
            SeedProperties properties,
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            NutritionGoalRepository nutritionGoalRepository,
            FoodEntryRepository foodEntryRepository,
            SavedFoodRepository savedFoodRepository,
            TrainingSessionRepository trainingSessionRepository,
            BodyMeasurementRepository bodyMeasurementRepository,
            UserProvisioningService userProvisioningService,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.nutritionGoalRepository = nutritionGoalRepository;
        this.foodEntryRepository = foodEntryRepository;
        this.savedFoodRepository = savedFoodRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
        this.userProvisioningService = userProvisioningService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (properties.getPassword() == null || properties.getPassword().isBlank()) {
            log.warn("SEED_ENABLED is true but SEED_PASSWORD is not set - skipping seed data");
            return;
        }
        String email = properties.getEmail().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("Seed user {} already exists - nothing to do", email);
            return;
        }

        User user = userRepository.save(
                new User(email, passwordEncoder.encode(properties.getPassword()), "Demo Athlete"));
        userProvisioningService.initialiseNewUser(user, "Europe/Berlin");

        ZoneId zone = ZoneId.of("Europe/Berlin");
        LocalDate today = LocalDate.now(clock.withZone(zone));
        int days = Math.clamp(properties.getDays(), 1, 365);

        configureProfile(user, today);
        seedSavedFoods(user);
        seedHistory(user, zone, today, days);

        log.info("Seeded demo user {} with {} days of history", email, days);
    }

    private void configureProfile(User user, LocalDate today) {
        UserSettings settings = userSettingsRepository.findByUserId(user.getId()).orElseThrow();
        settings.setSex(Sex.MALE);
        settings.setBirthDate(LocalDate.of(1994, 6, 12));
        settings.setHeightCm(new BigDecimal("181.0"));
        settings.setActivityLevel(ActivityLevel.ACTIVE);
        userSettingsRepository.save(settings);

        NutritionGoal goal = nutritionGoalRepository
                .findFirstByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(user.getId(), today)
                .orElseGet(() -> new NutritionGoal(user, today));
        goal.setCalorieTarget(2600);
        goal.setProteinTargetG(new BigDecimal("175.0"));
        goal.setCarbsTargetG(new BigDecimal("290.0"));
        goal.setFatTargetG(new BigDecimal("85.0"));
        goal.setFiberTargetG(new BigDecimal("32.0"));
        goal.setTargetBodyWeightKg(new BigDecimal("78.00"));
        nutritionGoalRepository.save(goal);
    }

    private void seedSavedFoods(User user) {
        record Preset(String name, String unit, BigDecimal serving, int kcal, double p, double c, double f, double fiber) {}
        List<Preset> presets = List.of(
                new Preset("Chicken breast", "g", new BigDecimal("100"), 165, 31, 0, 3.6, 0),
                new Preset("Basmati rice, cooked", "g", new BigDecimal("100"), 130, 2.6, 28, 0.3, 0.4),
                new Preset("Rolled oats", "g", new BigDecimal("100"), 370, 13, 60, 7, 10),
                new Preset("Whey protein", "scoop", BigDecimal.ONE, 120, 24, 3, 1.5, 0),
                new Preset("Olive oil", "g", new BigDecimal("10"), 88, 0, 0, 10, 0));

        for (Preset preset : presets) {
            SavedFood food = new SavedFood(user, preset.name());
            food.setServingQuantity(preset.serving());
            food.setServingUnit(preset.unit());
            food.setMacros(new Macros(
                    BigDecimal.valueOf(preset.kcal()),
                    BigDecimal.valueOf(preset.p()),
                    BigDecimal.valueOf(preset.c()),
                    BigDecimal.valueOf(preset.f()),
                    BigDecimal.valueOf(preset.fiber())));
            savedFoodRepository.save(food);
        }
    }

    private void seedHistory(User user, ZoneId zone, LocalDate today, int days) {
        Random random = new Random(RANDOM_SEED);
        BigDecimal weight = new BigDecimal("83.40");

        for (int offset = days - 1; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);

            addMeal(user, zone, date, 7, 30, "Oats, whey and berries", MealType.BREAKFAST,
                    620 + random.nextInt(80), 44, 78, 12, 11);
            addMeal(user, zone, date, 12, 45, "Chicken, rice and vegetables", MealType.LUNCH,
                    780 + random.nextInt(120), 62, 88, 18, 9);
            addMeal(user, zone, date, 19, 15, "Salmon, potatoes and salad", MealType.DINNER,
                    820 + random.nextInt(140), 55, 74, 32, 10);
            if (random.nextInt(3) > 0) {
                addMeal(user, zone, date, 16, 0, "Greek yoghurt and nuts", MealType.SNACK,
                        280 + random.nextInt(90), 22, 18, 14, 3);
            }

            // Training on roughly four days a week.
            if (offset % 7 != 2 && offset % 7 != 5 && random.nextInt(4) > 0) {
                addTraining(user, zone, date, random);
            }

            // Weigh-ins on most mornings, drifting gently downwards with day-to-day noise.
            if (random.nextInt(5) > 0) {
                BigDecimal noise = BigDecimal.valueOf((random.nextDouble() - 0.5) * 0.6);
                BigDecimal recorded = weight.add(noise).setScale(2, RoundingMode.HALF_UP);
                BodyMeasurement measurement = new BodyMeasurement(
                        user, date.atTime(6, 45).atZone(zone).toInstant(), recorded, MeasurementSource.MANUAL);
                bodyMeasurementRepository.save(measurement);
            }
            weight = weight.subtract(new BigDecimal("0.04"));
        }
    }

    private void addMeal(
            User user,
            ZoneId zone,
            LocalDate date,
            int hour,
            int minute,
            String name,
            MealType mealType,
            int calories,
            double protein,
            double carbs,
            double fat,
            double fiber) {

        Instant consumedAt = date.atTime(hour, minute).atZone(zone).toInstant();
        FoodEntry entry = new FoodEntry(user, name, consumedAt, date);
        entry.setMealType(mealType);
        entry.setSource(FoodEntrySource.MANUAL);
        entry.setMacros(new Macros(
                BigDecimal.valueOf(calories),
                BigDecimal.valueOf(protein),
                BigDecimal.valueOf(carbs),
                BigDecimal.valueOf(fat),
                BigDecimal.valueOf(fiber)));
        foodEntryRepository.save(entry);
    }

    private void addTraining(User user, ZoneId zone, LocalDate date, Random random) {
        record Template(String title, TrainingCategory category, int minutes, int kcal) {}
        List<Template> templates = List.of(
                new Template("Lower body", TrainingCategory.STRENGTH, 70, 540),
                new Template("Upper body", TrainingCategory.STRENGTH, 65, 500),
                new Template("Zone 2 run", TrainingCategory.RUNNING, 50, 470),
                new Template("Intervals", TrainingCategory.HIIT, 35, 420));

        Template template = templates.get(random.nextInt(templates.size()));
        Instant startedAt = date.atTime(17, 30).atZone(zone).toInstant();

        TrainingSession session = new TrainingSession(user, template.title(), startedAt, date);
        session.setCategory(template.category());
        session.setSource(TrainingSource.MANUAL);
        session.setDurationMinutes(template.minutes());
        session.setEndedAt(startedAt.plus(Duration.ofMinutes(template.minutes())));
        session.setPerceivedExertion(5 + random.nextInt(4));
        session.setCaloriesKcal(BigDecimal.valueOf(template.kcal() + random.nextInt(60)));
        trainingSessionRepository.save(session);
    }
}
