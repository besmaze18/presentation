package com.fittrack.training.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.training.dto.AnnotateTrainingSessionRequest;
import com.fittrack.training.dto.TrainingSessionResponse;
import com.fittrack.training.dto.UpsertTrainingSessionRequest;
import com.fittrack.training.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training")
@Tag(name = "Training", description = "Manual and imported training sessions")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/sessions")
    @Operation(summary = "Training sessions, most recent first, or for a single day")
    public List<TrainingSessionResponse> list(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        if (date != null) {
            return trainingService.forDay(currentUser.getId(), date);
        }
        return trainingService.list(currentUser.getId(), page, size);
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "A single session, including its exercises and sets")
    public TrainingSessionResponse get(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        return trainingService.get(currentUser.getId(), id);
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create a manual training session")
    public ResponseEntity<TrainingSessionResponse> create(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpsertTrainingSessionRequest request) {
        TrainingSessionResponse created = trainingService.create(currentUser.getId(), request);
        return ResponseEntity.created(URI.create("/api/training/sessions/" + created.id())).body(created);
    }

    @PutMapping("/sessions/{id}")
    @Operation(summary = "Replace a manually created session")
    public TrainingSessionResponse update(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpsertTrainingSessionRequest request) {
        return trainingService.update(currentUser.getId(), id, request);
    }

    @PatchMapping("/sessions/{id}")
    @Operation(summary = "Add a title, category, exertion rating or notes to any session")
    public TrainingSessionResponse annotate(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody AnnotateTrainingSessionRequest request) {
        return trainingService.annotate(currentUser.getId(), id, request);
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "Delete a manually created session")
    public ResponseEntity<Void> delete(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        trainingService.delete(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
