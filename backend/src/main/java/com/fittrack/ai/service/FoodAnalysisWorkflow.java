package com.fittrack.ai.service;

import com.fittrack.ai.domain.AiAnalysis;
import com.fittrack.ai.domain.AiAnalysisKind;
import com.fittrack.ai.domain.AiAnalysisRepository;
import com.fittrack.ai.domain.AiAnalysisStatus;
import com.fittrack.ai.dto.AiAnalysisResponse;
import com.fittrack.ai.dto.AnalyzedFoodItem;
import com.fittrack.ai.dto.ConfirmAnalysisRequest;
import com.fittrack.ai.dto.EstimateNutritionRequest;
import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.dto.ImageAnalysisRequest;
import com.fittrack.ai.dto.NutritionEstimateRequest;
import com.fittrack.ai.dto.TextAnalysisRequest;
import com.fittrack.common.exception.BadRequestException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.nutrition.domain.FoodEntry;
import com.fittrack.nutrition.domain.FoodEntrySource;
import com.fittrack.nutrition.domain.Macros;
import com.fittrack.nutrition.dto.CreateFoodEntryRequest;
import com.fittrack.nutrition.dto.FoodEntryResponse;
import com.fittrack.nutrition.dto.FoodItemDto;
import com.fittrack.nutrition.dto.MacrosDto;
import com.fittrack.nutrition.service.NutritionService;
import com.fittrack.storage.config.StorageProperties;
import com.fittrack.storage.service.StorageService;
import com.fittrack.storage.service.StorageUpload;
import com.fittrack.storage.service.StoredObject;
import com.fittrack.user.domain.User;
import com.fittrack.user.service.UserService;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The analyse → review → confirm workflow.
 *
 * <p>The key rule this class enforces is that an AI estimate never becomes nutrition on its own.
 * Analysis persists an {@link AiAnalysis} in {@code PENDING_REVIEW} and returns it; only an
 * explicit confirm call - carrying the values the user actually accepted - creates a
 * {@link FoodEntry}. Both the original prediction and the confirmed values are kept, so estimation
 * accuracy can be measured afterwards.
 */
@Service
public class FoodAnalysisWorkflow {

    private static final Logger log = LoggerFactory.getLogger(FoodAnalysisWorkflow.class);
    private static final String IMAGE_FOLDER_PREFIX = "meals";

    private final FoodAnalysisService foodAnalysisService;
    private final AiAnalysisRepository repository;
    private final AiAnalysisStore analysisStore;
    private final AiAnalysisMapper mapper;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final NutritionService nutritionService;
    private final UserService userService;
    private final Clock clock;

    public FoodAnalysisWorkflow(
            FoodAnalysisService foodAnalysisService,
            AiAnalysisRepository repository,
            AiAnalysisStore analysisStore,
            AiAnalysisMapper mapper,
            StorageService storageService,
            StorageProperties storageProperties,
            NutritionService nutritionService,
            UserService userService,
            Clock clock) {
        this.foodAnalysisService = foodAnalysisService;
        this.repository = repository;
        this.analysisStore = analysisStore;
        this.mapper = mapper;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
        this.nutritionService = nutritionService;
        this.userService = userService;
        this.clock = clock;
    }

    public boolean isAiAvailable() {
        return foodAnalysisService.isAvailable();
    }

    public String providerName() {
        return foodAnalysisService.providerName();
    }

    @Transactional
    public AiAnalysisResponse analyzeText(UUID userId, String description, String localeHint) {
        User user = userService.requireUser(userId);
        FoodAnalysisResult result =
                foodAnalysisService.analyzeText(new TextAnalysisRequest(description, localeHint));

        AiAnalysis analysis = new AiAnalysis(user, AiAnalysisKind.TEXT, result.providerName());
        analysis.setInputText(description);
        applyResult(analysis, result);
        return mapper.toResponse(repository.save(analysis), null);
    }

    @Transactional
    public AiAnalysisResponse estimate(UUID userId, EstimateNutritionRequest request) {
        User user = userService.requireUser(userId);
        FoodAnalysisResult result = foodAnalysisService.estimateNutrition(
                new NutritionEstimateRequest(request.foodName(), request.quantity(), request.unit()));

        AiAnalysis analysis = new AiAnalysis(user, AiAnalysisKind.ESTIMATE, result.providerName());
        analysis.setInputText(request.foodName());
        applyResult(analysis, result);
        return mapper.toResponse(repository.save(analysis), null);
    }

    /**
     * Uploads the photo, records the attempt, then analyses it.
     *
     * <p>Deliberately not {@code @Transactional}: each step commits separately through
     * {@link AiAnalysisStore}. A provider failure must leave the user with their picture and a
     * recorded FAILED attempt, and a single enclosing transaction would roll both away when the
     * exception propagates.
     */
    public AiAnalysisResponse analyzeImage(
            UUID userId, byte[] image, String contentType, String originalFilename, String userHint) {

        AiAnalysis pending = persistPendingImageAnalysis(
                userId, image, contentType, originalFilename, userHint);

        try {
            FoodAnalysisResult result = foodAnalysisService.analyzeImage(
                    new ImageAnalysisRequest(image, contentType, userHint, null));
            return completeImageAnalysis(pending.getId(), result);
        } catch (AiUnavailableException ex) {
            analysisStore.markFailed(pending.getId(), ex.getMessage());
            log.info("Image analysis failed for user {}; the image and the attempt were kept", userId);
            throw ex;
        }
    }

    private AiAnalysis persistPendingImageAnalysis(
            UUID userId, byte[] image, String contentType, String originalFilename, String userHint) {

        User user = userService.requireUser(userId);
        StoredObject stored = storageService.upload(new StorageUpload(
                image, contentType, originalFilename, IMAGE_FOLDER_PREFIX + "/" + userId));

        AiAnalysis analysis = new AiAnalysis(user, AiAnalysisKind.IMAGE, foodAnalysisService.providerName());
        analysis.setImageStorageProvider(stored.provider());
        analysis.setImageStorageKey(stored.key());
        analysis.setImageContentType(stored.contentType());
        analysis.setImageSizeBytes(stored.sizeBytes());
        analysis.setInputText(userHint);
        return analysisStore.save(analysis);
    }

    private AiAnalysisResponse completeImageAnalysis(UUID analysisId, FoodAnalysisResult result) {
        AiAnalysis analysis = repository.findById(analysisId).orElseThrow();
        applyResult(analysis, result);
        AiAnalysis saved = analysisStore.save(analysis);
        return mapper.toResponse(saved, imageUrl(saved));
    }

    @Transactional(readOnly = true)
    public AiAnalysisResponse get(UUID userId, UUID analysisId) {
        AiAnalysis analysis = require(userId, analysisId);
        return mapper.toResponse(analysis, imageUrl(analysis));
    }

    @Transactional(readOnly = true)
    public List<AiAnalysisResponse> recent(UUID userId, int limit) {
        return repository
                .findByUserIdOrderByCreatedAtDesc(
                        userId, org.springframework.data.domain.Limit.of(Math.clamp(limit, 1, 50)))
                .stream()
                .map(analysis -> mapper.toResponse(analysis, imageUrl(analysis)))
                .toList();
    }

    /**
     * Turns a reviewed prediction into a real nutrition entry using the values the user accepted.
     */
    @Transactional
    public FoodEntryResponse confirm(UUID userId, UUID analysisId, ConfirmAnalysisRequest request) {
        AiAnalysis analysis = require(userId, analysisId);

        if (analysis.getStatus() == AiAnalysisStatus.CONFIRMED) {
            throw new BadRequestException("This analysis has already been confirmed");
        }
        if (analysis.getStatus() == AiAnalysisStatus.FAILED) {
            throw new BadRequestException("This analysis failed and cannot be confirmed");
        }
        if ((request.items() == null || request.items().isEmpty()) && request.macros() == null) {
            throw new BadRequestException("Either 'macros' or a non-empty 'items' list is required");
        }

        CreateFoodEntryRequest create = new CreateFoodEntryRequest(
                request.name(),
                request.mealType(),
                request.consumedAt(),
                null,
                null,
                request.macros(),
                request.items(),
                request.notes(),
                analysis.getId());

        FoodEntrySource source = analysis.getKind() == AiAnalysisKind.IMAGE
                ? FoodEntrySource.AI_IMAGE
                : FoodEntrySource.AI_TEXT;
        FoodEntry entry = nutritionService.createEntry(userId, create, source, null);

        // Record what the user actually accepted next to what was predicted.
        analysis.markConfirmed(entry.getId(), entry.getMacros(), clock.instant());
        repository.save(analysis);

        log.debug("Analysis {} confirmed as food entry {}", analysis.getId(), entry.getId());
        return FoodEntryResponse.from(entry);
    }

    @Transactional
    public void discard(UUID userId, UUID analysisId) {
        AiAnalysis analysis = require(userId, analysisId);
        if (analysis.getStatus() == AiAnalysisStatus.CONFIRMED) {
            throw new BadRequestException("A confirmed analysis cannot be discarded");
        }
        analysis.markDiscarded();
        // The stored image is removed with the discarded prediction so unreviewed photos do not
        // accumulate in object storage.
        if (analysis.getImageStorageKey() != null) {
            try {
                storageService.delete(analysis.getImageStorageKey());
            } catch (RuntimeException ex) {
                log.warn("Could not delete stored image for discarded analysis {}", analysis.getId());
            }
        }
        repository.save(analysis);
    }

    private AiAnalysis require(UUID userId, UUID analysisId) {
        return repository
                .findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> NotFoundException.of("Analysis", analysisId));
    }

    private String imageUrl(AiAnalysis analysis) {
        if (analysis.getImageStorageKey() == null) {
            return null;
        }
        try {
            return storageService.accessibleUrl(
                    analysis.getImageStorageKey(), storageProperties.getSignedUrlTtl());
        } catch (RuntimeException ex) {
            log.warn("Could not build an image URL for analysis {}", analysis.getId());
            return null;
        }
    }

    private void applyResult(AiAnalysis analysis, FoodAnalysisResult result) {
        analysis.setModel(result.model());
        analysis.setPredictedMealName(result.mealName());
        analysis.setPredictedMacros(new Macros(
                result.totalCalories(),
                result.totalProteinG(),
                result.totalCarbsG(),
                result.totalFatG(),
                result.totalFiberG()));
        analysis.setConfidence(result.confidence());
        analysis.setPrediction(mapper.toJson(normalisePrediction(result)));
        analysis.setRawResponse(rawAsJson(result.rawResponseJson()));
        analysis.setLatencyMillis(result.latencyMillis());
        analysis.setInputTokens(result.inputTokens());
        analysis.setOutputTokens(result.outputTokens());
    }

    /** Normalises the provider's field names into the shape the client and mapper expect. */
    private Map<String, Object> normalisePrediction(FoodAnalysisResult result) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (AnalyzedFoodItem item : result.items()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("name", item.name());
            node.put("quantity", item.quantity());
            node.put("unit", item.unit());
            node.put("calories", item.calories());
            node.put("proteinG", item.proteinG());
            node.put("carbsG", item.carbsG());
            node.put("fatG", item.fatG());
            node.put("fiberG", item.fiberG());
            items.add(node);
        }

        Map<String, Object> prediction = new LinkedHashMap<>();
        prediction.put("mealName", result.mealName());
        prediction.put("items", items);
        prediction.put("assumptions", result.assumptions());
        prediction.put("notes", result.notes());
        prediction.put("confidence", result.confidence());
        return prediction;
    }

    private com.fasterxml.jackson.databind.JsonNode rawAsJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            return mapper.toJson(
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(rawJson));
        } catch (Exception ex) {
            // Keep the text verbatim when it is not parseable JSON, so nothing is lost.
            return mapper.toJson(Map.of("text", rawJson));
        }
    }

    /** Convenience for building a confirm request straight from a prediction, used by tests. */
    static MacrosDto totalsOf(List<FoodItemDto> items) {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carbs = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal fiber = BigDecimal.ZERO;
        for (FoodItemDto item : items) {
            calories = calories.add(item.macros().calories());
            protein = protein.add(item.macros().proteinG());
            carbs = carbs.add(item.macros().carbsG());
            fat = fat.add(item.macros().fatG());
            fiber = fiber.add(item.macros().fiberG());
        }
        return new MacrosDto(calories, protein, carbs, fat, fiber);
    }
}
