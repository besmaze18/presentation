package com.fittrack.ai.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.common.domain.BaseEntity;
import com.fittrack.nutrition.domain.Macros;
import com.fittrack.user.domain.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One AI food-analysis attempt and its outcome.
 *
 * <p>The prediction and the user's confirmed values are stored side by side, together with the
 * model's self-reported confidence, the image reference and timing. That pairing is the whole
 * point of the table: it is what makes it possible to measure later how accurate the estimates
 * actually were, per model and per meal type.
 *
 * <p>An analysis is never itself a nutrition entry. It becomes one only when the user confirms it,
 * at which point {@code confirmedFoodEntryId} links the two.
 */
@Entity
@Table(name = "ai_analyses")
public class AiAnalysis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private AiAnalysisKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private AiAnalysisStatus status = AiAnalysisStatus.PENDING_REVIEW;

    @Column(name = "provider", nullable = false, length = 64)
    private String provider;

    @Column(name = "model", length = 128)
    private String model;

    @Column(name = "input_text", length = 2000)
    private String inputText;

    // Only the reference and metadata live in PostgreSQL - never the image bytes.
    @Column(name = "image_storage_provider", length = 32)
    private String imageStorageProvider;

    @Column(name = "image_storage_key", length = 512)
    private String imageStorageKey;

    @Column(name = "image_content_type", length = 64)
    private String imageContentType;

    @Column(name = "image_size_bytes")
    private Long imageSizeBytes;

    @Column(name = "predicted_meal_name", length = 200)
    private String predictedMealName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calories", column = @Column(name = "predicted_calories")),
        @AttributeOverride(name = "proteinG", column = @Column(name = "predicted_protein_g")),
        @AttributeOverride(name = "carbsG", column = @Column(name = "predicted_carbs_g")),
        @AttributeOverride(name = "fatG", column = @Column(name = "predicted_fat_g")),
        @AttributeOverride(name = "fiberG", column = @Column(name = "predicted_fiber_g"))
    })
    private Macros predictedMacros = Macros.zero();

    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;

    /** The normalised prediction: detected items, quantities, assumptions and notes. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prediction")
    private JsonNode prediction;

    /** The provider's untouched response, kept for auditing and future accuracy analysis. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_response")
    private JsonNode rawResponse;

    @Column(name = "latency_millis")
    private Long latencyMillis;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "confirmed_food_entry_id")
    private UUID confirmedFoodEntryId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "calories", column = @Column(name = "confirmed_calories")),
        @AttributeOverride(name = "proteinG", column = @Column(name = "confirmed_protein_g")),
        @AttributeOverride(name = "carbsG", column = @Column(name = "confirmed_carbs_g")),
        @AttributeOverride(name = "fatG", column = @Column(name = "confirmed_fat_g")),
        @AttributeOverride(name = "fiberG", column = @Column(name = "confirmed_fiber_g"))
    })
    private Macros confirmedMacros;

    protected AiAnalysis() {}

    public AiAnalysis(User user, AiAnalysisKind kind, String provider) {
        this.user = user;
        this.kind = kind;
        this.provider = provider;
    }

    public void markConfirmed(UUID foodEntryId, Macros confirmed, Instant at) {
        this.status = AiAnalysisStatus.CONFIRMED;
        this.confirmedFoodEntryId = foodEntryId;
        this.confirmedMacros = confirmed;
        this.confirmedAt = at;
    }

    public void markDiscarded() {
        this.status = AiAnalysisStatus.DISCARDED;
    }

    public void markFailed(String message) {
        this.status = AiAnalysisStatus.FAILED;
        this.errorMessage = message;
    }

    public User getUser() {
        return user;
    }

    public AiAnalysisKind getKind() {
        return kind;
    }

    public AiAnalysisStatus getStatus() {
        return status;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getImageStorageProvider() {
        return imageStorageProvider;
    }

    public void setImageStorageProvider(String imageStorageProvider) {
        this.imageStorageProvider = imageStorageProvider;
    }

    public String getImageStorageKey() {
        return imageStorageKey;
    }

    public void setImageStorageKey(String imageStorageKey) {
        this.imageStorageKey = imageStorageKey;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public void setImageContentType(String imageContentType) {
        this.imageContentType = imageContentType;
    }

    public Long getImageSizeBytes() {
        return imageSizeBytes;
    }

    public void setImageSizeBytes(Long imageSizeBytes) {
        this.imageSizeBytes = imageSizeBytes;
    }

    public String getPredictedMealName() {
        return predictedMealName;
    }

    public void setPredictedMealName(String predictedMealName) {
        this.predictedMealName = predictedMealName;
    }

    public Macros getPredictedMacros() {
        return predictedMacros;
    }

    public void setPredictedMacros(Macros predictedMacros) {
        this.predictedMacros = predictedMacros;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public JsonNode getPrediction() {
        return prediction;
    }

    public void setPrediction(JsonNode prediction) {
        this.prediction = prediction;
    }

    public JsonNode getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(JsonNode rawResponse) {
        this.rawResponse = rawResponse;
    }

    public Long getLatencyMillis() {
        return latencyMillis;
    }

    public void setLatencyMillis(Long latencyMillis) {
        this.latencyMillis = latencyMillis;
    }

    public Long getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(Long inputTokens) {
        this.inputTokens = inputTokens;
    }

    public Long getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(Long outputTokens) {
        this.outputTokens = outputTokens;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public UUID getConfirmedFoodEntryId() {
        return confirmedFoodEntryId;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Macros getConfirmedMacros() {
        return confirmedMacros;
    }
}
