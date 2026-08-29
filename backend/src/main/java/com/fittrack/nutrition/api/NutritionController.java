package com.fittrack.nutrition.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.nutrition.dto.CreateFoodEntryRequest;
import com.fittrack.nutrition.dto.DailyNutritionResponse;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.nutrition.dto.UpdateFoodEntryRequest;
import com.fittrack.nutrition.service.NutritionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition")
@Tag(name = "Nutrition", description = "Logged meals and daily macro totals")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/days/{date}")
    @Operation(summary = "Every entry plus the totals for one calendar day")
    public DailyNutritionResponse day(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return nutritionService.day(currentUser.getId(), date);
    }

    @GetMapping("/entries")
    @Operation(summary = "Entries for a day, defaulting to today in the user's time zone")
    public DailyNutritionResponse entries(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return nutritionService.day(currentUser.getId(), date);
    }

    @GetMapping("/entries/{id}")
    @Operation(summary = "A single logged entry")
    public FoodEntryResponse get(@CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        return nutritionService.get(currentUser.getId(), id);
    }

    @PostMapping("/entries")
    @Operation(summary = "Log a meal from manually entered macros")
    public ResponseEntity<FoodEntryResponse> create(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody CreateFoodEntryRequest request) {
        FoodEntryResponse created = nutritionService.create(currentUser.getId(), request);
        return ResponseEntity.created(URI.create("/api/nutrition/entries/" + created.id())).body(created);
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Replace a logged entry")
    public FoodEntryResponse update(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFoodEntryRequest request) {
        return nutritionService.update(currentUser.getId(), id, request);
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete a logged entry")
    public ResponseEntity<Void> delete(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        nutritionService.delete(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
