package com.fittrack.nutrition.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.nutrition.dto.LogSavedFoodRequest;
import com.fittrack.nutrition.dto.SavedFoodResponse;
import com.fittrack.nutrition.dto.UpsertSavedFoodRequest;
import com.fittrack.nutrition.service.SavedFoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/foods")
@Tag(name = "Saved foods", description = "Reusable foods and meals")
public class SavedFoodController {

    private final SavedFoodService savedFoodService;

    public SavedFoodController(SavedFoodService savedFoodService) {
        this.savedFoodService = savedFoodService;
    }

    @GetMapping
    @Operation(summary = "Search saved foods, most frequently used first")
    public List<SavedFoodResponse> search(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "25") int limit) {
        return savedFoodService.search(currentUser.getId(), query, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "A single saved food")
    public SavedFoodResponse get(@CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        return savedFoodService.get(currentUser.getId(), id);
    }

    @PostMapping
    @Operation(summary = "Save a food for reuse")
    public ResponseEntity<SavedFoodResponse> create(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpsertSavedFoodRequest request) {
        SavedFoodResponse created = savedFoodService.create(currentUser.getId(), request);
        return ResponseEntity.created(URI.create("/api/foods/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a saved food")
    public SavedFoodResponse update(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpsertSavedFoodRequest request) {
        return savedFoodService.update(currentUser.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a saved food")
    public ResponseEntity<Void> delete(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        savedFoodService.delete(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/log")
    @Operation(summary = "Log a saved food, scaling its macros to the requested quantity")
    public ResponseEntity<FoodEntryResponse> log(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody LogSavedFoodRequest request) {
        FoodEntryResponse created = savedFoodService.logEntry(currentUser.getId(), id, request);
        return ResponseEntity.created(URI.create("/api/nutrition/entries/" + created.id())).body(created);
    }
}
