package com.fittrack.nutrition.service;

import com.fittrack.common.exception.ConflictException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.nutrition.domain.FoodEntry;
import com.fittrack.nutrition.domain.FoodEntrySource;
import com.fittrack.nutrition.domain.Macros;
import com.fittrack.nutrition.domain.MealType;
import com.fittrack.nutrition.domain.SavedFood;
import com.fittrack.nutrition.domain.SavedFoodRepository;
import com.fittrack.nutrition.dto.CreateFoodEntryRequest;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.nutrition.dto.LogSavedFoodRequest;
import com.fittrack.nutrition.dto.MacrosDto;
import com.fittrack.nutrition.dto.SavedFoodResponse;
import com.fittrack.nutrition.dto.UpsertSavedFoodRequest;
import com.fittrack.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reusable foods: the fastest path to logging something eaten regularly. */
@Service
public class SavedFoodService {

    private static final int MAX_SEARCH_RESULTS = 50;

    private final SavedFoodRepository savedFoodRepository;
    private final NutritionService nutritionService;
    private final UserService userService;
    private final Clock clock;

    public SavedFoodService(
            SavedFoodRepository savedFoodRepository,
            NutritionService nutritionService,
            UserService userService,
            Clock clock) {
        this.savedFoodRepository = savedFoodRepository;
        this.nutritionService = nutritionService;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SavedFoodResponse> search(UUID userId, String query, int limit) {
        String normalised = query == null || query.isBlank() ? null : query.trim();
        return savedFoodRepository
                .search(userId, normalised, PageRequest.of(0, Math.clamp(limit, 1, MAX_SEARCH_RESULTS)))
                .stream()
                .map(SavedFoodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SavedFoodResponse get(UUID userId, UUID id) {
        return SavedFoodResponse.from(require(userId, id));
    }

    @Transactional
    public SavedFoodResponse create(UUID userId, UpsertSavedFoodRequest request) {
        if (savedFoodRepository.existsByUserIdAndNameIgnoreCase(userId, request.name().trim())) {
            throw new ConflictException("You already saved a food called '" + request.name().trim() + "'");
        }
        SavedFood food = new SavedFood(userService.requireUser(userId), request.name().trim());
        apply(food, request);
        return SavedFoodResponse.from(savedFoodRepository.save(food));
    }

    @Transactional
    public SavedFoodResponse update(UUID userId, UUID id, UpsertSavedFoodRequest request) {
        SavedFood food = require(userId, id);
        apply(food, request);
        return SavedFoodResponse.from(savedFoodRepository.save(food));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        savedFoodRepository.delete(require(userId, id));
    }

    /**
     * Logs a saved food at an arbitrary quantity by scaling the stored per-serving macros. The
     * resulting entry is a normal food entry, so editing or deleting it later needs no special case.
     */
    @Transactional
    public FoodEntryResponse logEntry(UUID userId, UUID savedFoodId, LogSavedFoodRequest request) {
        SavedFood food = require(userId, savedFoodId);

        BigDecimal servingQuantity = food.getServingQuantity();
        BigDecimal factor = servingQuantity == null || servingQuantity.signum() == 0
                ? BigDecimal.ONE
                : request.quantity().divide(servingQuantity, 6, RoundingMode.HALF_UP);
        Macros scaled = food.getMacros().scaled(factor);

        MealType mealType = request.mealType() != null
                ? request.mealType()
                : food.getDefaultMealType() != null ? food.getDefaultMealType() : MealType.OTHER;

        CreateFoodEntryRequest create = new CreateFoodEntryRequest(
                food.getName(),
                mealType,
                request.consumedAt(),
                request.quantity(),
                food.getServingUnit(),
                MacrosDto.from(scaled),
                null,
                null,
                null);

        FoodEntry entry =
                nutritionService.createEntry(userId, create, FoodEntrySource.SAVED_FOOD, food.getId());
        food.recordUsage(clock.instant());
        savedFoodRepository.save(food);
        return FoodEntryResponse.from(entry);
    }

    private SavedFood require(UUID userId, UUID id) {
        return savedFoodRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundException.of("Saved food", id));
    }

    private void apply(SavedFood food, UpsertSavedFoodRequest request) {
        food.setName(request.name().trim());
        food.setBrand(request.brand() == null || request.brand().isBlank() ? null : request.brand().trim());
        food.setServingQuantity(request.servingQuantity());
        food.setServingUnit(request.servingUnit().trim());
        food.setMacros(request.macros().toMacros());
        food.setDefaultMealType(request.defaultMealType());
    }
}
