package com.fittrack.ai.api;

import com.fittrack.ai.dto.AiAnalysisResponse;
import com.fittrack.ai.dto.AnalyzeTextRequest;
import com.fittrack.ai.dto.ConfirmAnalysisRequest;
import com.fittrack.ai.dto.EstimateNutritionRequest;
import com.fittrack.ai.service.FoodAnalysisWorkflow;
import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.storage.service.ImageValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI food analysis.
 *
 * <p>Every analysis endpoint returns a <em>proposal</em>. Nothing here writes nutrition: the client
 * shows the proposal on an editable review screen and calls
 * {@code POST /api/ai/food-analysis/{id}/confirm} with the values the user accepted.
 */
@RestController
@RequestMapping("/api/ai/food-analysis")
@Tag(name = "AI food analysis", description = "Natural-language and photo analysis, reviewed before saving")
public class FoodAnalysisController {

    private final FoodAnalysisWorkflow workflow;
    private final ImageValidator imageValidator;

    public FoodAnalysisController(FoodAnalysisWorkflow workflow, ImageValidator imageValidator) {
        this.workflow = workflow;
        this.imageValidator = imageValidator;
    }

    @GetMapping("/status")
    @Operation(summary = "Whether AI analysis is configured, so the UI can hide it if not")
    public Map<String, Object> status() {
        return Map.of("available", workflow.isAiAvailable(), "provider", workflow.providerName());
    }

    @PostMapping("/text")
    @Operation(summary = "Analyse a natural-language meal description")
    public ResponseEntity<AiAnalysisResponse> analyzeText(
            @CurrentUser AuthenticatedUser currentUser, @Valid @RequestBody AnalyzeTextRequest request) {
        AiAnalysisResponse analysis =
                workflow.analyzeText(currentUser.getId(), request.description(), request.localeHint());
        return created(analysis);
    }

    @PostMapping("/estimate")
    @Operation(summary = "Estimate macros for one named food at a given quantity")
    public ResponseEntity<AiAnalysisResponse> estimate(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody EstimateNutritionRequest request) {
        return created(workflow.estimate(currentUser.getId(), request));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Analyse a meal photograph",
            description =
                    "The image is validated by its magic bytes, stored through the StorageService "
                            + "abstraction, then analysed. The response is a proposal awaiting review.")
    public ResponseEntity<AiAnalysisResponse> analyzeImage(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestPart("image") MultipartFile image,
            @RequestParam(required = false) String hint)
            throws IOException {

        String contentType = imageValidator.validate(image);
        AiAnalysisResponse analysis = workflow.analyzeImage(
                currentUser.getId(),
                image.getBytes(),
                contentType,
                image.getOriginalFilename(),
                hint);
        return created(analysis);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a stored analysis and its review payload")
    public AiAnalysisResponse get(@CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        return workflow.get(currentUser.getId(), id);
    }

    @GetMapping
    @Operation(summary = "Recent analyses for the authenticated user")
    public List<AiAnalysisResponse> recent(
            @CurrentUser AuthenticatedUser currentUser, @RequestParam(defaultValue = "20") int limit) {
        return workflow.recent(currentUser.getId(), limit);
    }

    @PostMapping("/{id}/confirm")
    @Operation(
            summary = "Confirm a reviewed analysis, creating the nutrition entry",
            description = "The values in this request - not the prediction - are what get saved.")
    public ResponseEntity<FoodEntryResponse> confirm(
            @CurrentUser AuthenticatedUser currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmAnalysisRequest request) {
        FoodEntryResponse entry = workflow.confirm(currentUser.getId(), id, request);
        return ResponseEntity.created(URI.create("/api/nutrition/entries/" + entry.id())).body(entry);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Discard an unconfirmed analysis and its stored image")
    public ResponseEntity<Void> discard(
            @CurrentUser AuthenticatedUser currentUser, @PathVariable UUID id) {
        workflow.discard(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<AiAnalysisResponse> created(AiAnalysisResponse analysis) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/ai/food-analysis/" + analysis.id()))
                .body(analysis);
    }
}
