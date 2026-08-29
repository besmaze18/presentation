package com.fittrack.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.domain.AiAnalysis;
import com.fittrack.ai.dto.AiAnalysisResponse;
import com.fittrack.nutrition.dto.FoodItemDto;
import com.fittrack.nutrition.dto.MacrosDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Turns a stored analysis back into the review payload the client renders. */
@Component
public class AiAnalysisMapper {

    private final ObjectMapper objectMapper;

    public AiAnalysisMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiAnalysisResponse toResponse(AiAnalysis analysis, String imageUrl) {
        JsonNode prediction = analysis.getPrediction();
        return new AiAnalysisResponse(
                analysis.getId(),
                analysis.getKind().name(),
                analysis.getStatus().name(),
                analysis.getProvider(),
                analysis.getModel(),
                analysis.getPredictedMealName(),
                items(prediction),
                MacrosDto.from(analysis.getPredictedMacros()),
                analysis.getConfidence(),
                assumptions(prediction),
                prediction == null ? null : textOrNull(prediction.path("notes")),
                imageUrl,
                analysis.getLatencyMillis(),
                analysis.getConfirmedFoodEntryId(),
                analysis.getCreatedAt());
    }

    private List<FoodItemDto> items(JsonNode prediction) {
        if (prediction == null || !prediction.path("items").isArray()) {
            return List.of();
        }
        List<FoodItemDto> items = new ArrayList<>();
        for (JsonNode node : prediction.path("items")) {
            items.add(new FoodItemDto(
                    node.path("name").asText(""),
                    decimal(node, "quantity"),
                    textOrNull(node.path("unit")),
                    new MacrosDto(
                            decimalOrZero(node, "calories"),
                            decimalOrZero(node, "proteinG"),
                            decimalOrZero(node, "carbsG"),
                            decimalOrZero(node, "fatG"),
                            decimalOrZero(node, "fiberG"))));
        }
        return items;
    }

    private List<String> assumptions(JsonNode prediction) {
        if (prediction == null || !prediction.path("assumptions").isArray()) {
            return List.of();
        }
        List<String> assumptions = new ArrayList<>();
        for (JsonNode node : prediction.path("assumptions")) {
            if (node.isTextual()) {
                assumptions.add(node.asText());
            }
        }
        return assumptions;
    }

    public JsonNode toJson(Object value) {
        return objectMapper.valueToTree(value);
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.decimalValue() : null;
    }

    private static BigDecimal decimalOrZero(JsonNode node, String field) {
        BigDecimal value = decimal(node, field);
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String textOrNull(JsonNode node) {
        return node.isTextual() && !node.asText().isBlank() ? node.asText() : null;
    }
}
