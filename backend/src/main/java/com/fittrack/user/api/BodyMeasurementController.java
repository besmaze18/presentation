package com.fittrack.user.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.user.domain.NutritionGoal;
import com.fittrack.user.dto.BodyMeasurementResponse;
import com.fittrack.user.dto.UpsertBodyMeasurementRequest;
import com.fittrack.user.dto.WeightTrendResponse;
import com.fittrack.user.service.BodyMeasurementService;
import com.fittrack.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
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
@RequestMapping("/api/body-measurements")
@Tag(name = "Body measurements", description = "Body weight history and trends")
public class BodyMeasurementController {

    private final BodyMeasurementService bodyMeasurementService;
    private final UserService userService;
    private final Clock clock;

    public BodyMeasurementController(
            BodyMeasurementService bodyMeasurementService, UserService userService, Clock clock) {
        this.bodyMeasurementService = bodyMeasurementService;
        this.userService = userService;
        this.clock = clock;
    }

    @GetMapping
    @Operation(summary = "Recorded measurements, most recent first")
    public List<BodyMeasurementResponse> list(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return bodyMeasurementService.list(currentUser.getId(), page, size).getContent();
    }

    @GetMapping("/trend")
    @Operation(summary = "Latest weight plus 7- and 30-day rolling averages and their change")
    public WeightTrendResponse trend(@CurrentUser AuthenticatedUser currentUser) {
        UUID userId = currentUser.getId();
        ZoneId zone = userService.zoneOf(userId);
        LocalDate today = LocalDate.now(clock.withZone(zone));
        BigDecimal target = targetWeight(userId, today);
        return bodyMeasurementService.trend(userId, today, zone, target);
    }

    @PostMapping
    @Operation(summary = "Record a body weight")
    public ResponseEntity<BodyMeasurementResponse> create(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpsertBodyMeasurementRequest request) {
        BodyMeasurementResponse created = bodyMeasurementService.record(currentUser.getId(), request);
        return ResponseEntity.created(URI.create("/api/body-measurements/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a recorded measurement")
    public BodyMeasurementResponse update(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpsertBodyMeasurementRequest request) {
        return bodyMeasurementService.update(currentUser.getId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recorded measurement")
    public ResponseEntity<Void> delete(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        bodyMeasurementService.delete(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private BigDecimal targetWeight(UUID userId, LocalDate today) {
        try {
            NutritionGoal goal = userService.goalInEffect(userId, today);
            return goal.getTargetBodyWeightKg();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
