package com.fittrack.nutrition.service;

import com.fittrack.common.exception.BadRequestException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.nutrition.domain.DailyMacroTotals;
import com.fittrack.nutrition.domain.FoodEntry;
import com.fittrack.nutrition.domain.FoodEntryRepository;
import com.fittrack.nutrition.domain.FoodEntrySource;
import com.fittrack.nutrition.domain.FoodItem;
import com.fittrack.nutrition.domain.Macros;
import com.fittrack.nutrition.domain.MealType;
import com.fittrack.nutrition.dto.CreateFoodEntryRequest;
import com.fittrack.nutrition.dto.DailyNutritionResponse;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.nutrition.dto.FoodItemDto;
import com.fittrack.nutrition.dto.MacrosDto;
import com.fittrack.nutrition.dto.UpdateFoodEntryRequest;
import com.fittrack.user.domain.User;
import com.fittrack.user.service.UserService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creating, reading, updating and deleting logged meals. */
@Service
public class NutritionService {

    private static final Logger log = LoggerFactory.getLogger(NutritionService.class);

    private final FoodEntryRepository foodEntryRepository;
    private final UserService userService;
    private final Clock clock;

    public NutritionService(
            FoodEntryRepository foodEntryRepository, UserService userService, Clock clock) {
        this.foodEntryRepository = foodEntryRepository;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public FoodEntryResponse create(UUID userId, CreateFoodEntryRequest request) {
        return FoodEntryResponse.from(createEntry(userId, request, FoodEntrySource.MANUAL, null));
    }

    /**
     * Shared creation path so manual entries, saved-food logs and confirmed AI predictions all
     * produce structurally identical rows - only the recorded source differs.
     */
    @Transactional
    public FoodEntry createEntry(
            UUID userId, CreateFoodEntryRequest request, FoodEntrySource source, UUID savedFoodId) {
        User user = userService.requireUser(userId);
        ZoneId zone = userService.zoneOf(userId);
        Instant consumedAt = request.consumedAt() != null ? request.consumedAt() : clock.instant();

        FoodEntry entry = new FoodEntry(
                user, request.name().trim(), consumedAt, LocalDate.ofInstant(consumedAt, zone));
        entry.setMealType(request.mealType() != null ? request.mealType() : MealType.OTHER);
        entry.setQuantity(request.quantity());
        entry.setUnit(trimToNull(request.unit()));
        entry.setNotes(trimToNull(request.notes()));
        entry.setSource(source);
        entry.setSavedFoodId(savedFoodId);
        entry.setAiAnalysisId(request.aiAnalysisId());

        applyMacrosAndItems(entry, request.macros(), request.items());

        FoodEntry saved = foodEntryRepository.save(entry);
        log.debug("Created food entry {} for user {}", saved.getId(), userId);
        return saved;
    }

    @Transactional
    public FoodEntryResponse update(UUID userId, UUID entryId, UpdateFoodEntryRequest request) {
        FoodEntry entry = requireEntry(userId, entryId);
        ZoneId zone = userService.zoneOf(userId);

        entry.setName(request.name().trim());
        entry.setMealType(request.mealType());
        entry.setConsumedAt(request.consumedAt());
        entry.setEntryDate(LocalDate.ofInstant(request.consumedAt(), zone));
        entry.setQuantity(request.quantity());
        entry.setUnit(trimToNull(request.unit()));
        entry.setNotes(trimToNull(request.notes()));

        applyMacrosAndItems(entry, request.macros(), request.items());

        return FoodEntryResponse.from(foodEntryRepository.save(entry));
    }

    @Transactional
    public void delete(UUID userId, UUID entryId) {
        foodEntryRepository.delete(requireEntry(userId, entryId));
    }

    @Transactional(readOnly = true)
    public FoodEntryResponse get(UUID userId, UUID entryId) {
        return FoodEntryResponse.from(requireEntry(userId, entryId));
    }

    @Transactional(readOnly = true)
    public DailyNutritionResponse day(UUID userId, LocalDate date) {
        LocalDate resolved = date != null ? date : today(userId);
        List<FoodEntry> entries =
                foodEntryRepository.findByUserIdAndEntryDateOrderByConsumedAtAsc(userId, resolved);

        Macros totals = Macros.zero();
        for (FoodEntry entry : entries) {
            totals = totals.plus(entry.getMacros());
        }

        return new DailyNutritionResponse(
                resolved,
                MacrosDto.from(totals),
                entries.size(),
                entries.stream().map(FoodEntryResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public List<DailyMacroTotals> aggregateByDay(UUID userId, LocalDate from, LocalDate to) {
        return foodEntryRepository.aggregateByDay(userId, from, to);
    }

    @Transactional(readOnly = true)
    public DailyMacroTotals totalsForDay(UUID userId, LocalDate date) {
        DailyMacroTotals totals = foodEntryRepository.aggregateForDay(userId, date);
        return totals == null ? DailyMacroTotals.empty(date) : totals;
    }

    @Transactional(readOnly = true)
    public LocalDate today(UUID userId) {
        return LocalDate.now(clock.withZone(userService.zoneOf(userId)));
    }

    private FoodEntry requireEntry(UUID userId, UUID entryId) {
        return foodEntryRepository
                .findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> NotFoundException.of("Food entry", entryId));
    }

    private void applyMacrosAndItems(FoodEntry entry, MacrosDto macros, List<FoodItemDto> items) {
        if (items != null && !items.isEmpty()) {
            List<FoodItem> entities = items.stream().map(FoodItemDto::toEntity).toList();
            entry.replaceItems(entities);
            entry.recalculateTotalsFromItems();
            return;
        }
        if (macros == null) {
            throw new BadRequestException("Either 'macros' or a non-empty 'items' list is required");
        }
        entry.replaceItems(List.of());
        entry.setMacros(macros.toMacros());
    }

    static BigDecimal safeQuantity(BigDecimal value) {
        return value == null ? BigDecimal.ONE : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
