package com.fittrack.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.dto.AnalyzedFoodItem;
import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.service.AiUnavailableException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Parses and validates a model's JSON before it is allowed anywhere near the database.
 *
 * <p>Validation is deliberately strict: a response with no items, a negative macro or an
 * implausible calorie figure is rejected outright rather than silently corrected, because a
 * plausible-looking wrong number is worse than an honest failure the user can work around by
 * entering the meal manually.
 */
@Component
public class FoodAnalysisResponseParser {

    /** Above this, the "meal" is certainly a parsing error rather than food. */
    private static final BigDecimal MAX_PLAUSIBLE_CALORIES = new BigDecimal("20000");

    private static final BigDecimal MAX_PLAUSIBLE_MACRO_GRAMS = new BigDecimal("2000");

    private static final int MAX_ITEMS = 40;

    private final ObjectMapper objectMapper;

    public FoodAnalysisResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FoodAnalysisResult parse(
            String rawJson,
            String providerName,
            String model,
            Long latencyMillis,
            Long inputTokens,
            Long outputTokens) {

        JsonNode root;
        try {
            root = objectMapper.readTree(extractJsonObject(rawJson));
        } catch (Exception ex) {
            throw new AiUnavailableException("The analysis service returned a response we could not read");
        }

        if (!root.isObject()) {
            throw new AiUnavailableException("The analysis service returned an unexpected response shape");
        }

        JsonNode itemsNode = root.path("items");
        if (!itemsNode.isArray() || itemsNode.isEmpty()) {
            throw new AiUnavailableException("No food could be identified");
        }
        if (itemsNode.size() > MAX_ITEMS) {
            throw new AiUnavailableException("The analysis returned an implausible number of items");
        }

        List<AnalyzedFoodItem> items = new ArrayList<>();
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;

        for (JsonNode itemNode : itemsNode) {
            String name = text(itemNode, "name");
            if (name == null || name.isBlank()) {
                throw new AiUnavailableException("The analysis returned an unnamed food item");
            }
            BigDecimal calories = macro(itemNode, "calories", MAX_PLAUSIBLE_CALORIES);
            BigDecimal protein = macro(itemNode, "protein_g", MAX_PLAUSIBLE_MACRO_GRAMS);
            BigDecimal carbs = macro(itemNode, "carbs_g", MAX_PLAUSIBLE_MACRO_GRAMS);
            BigDecimal fat = macro(itemNode, "fat_g", MAX_PLAUSIBLE_MACRO_GRAMS);
            BigDecimal fiber = macro(itemNode, "fiber_g", MAX_PLAUSIBLE_MACRO_GRAMS);

            items.add(new AnalyzedFoodItem(
                    truncate(name, 200),
                    optionalNumber(itemNode, "quantity"),
                    truncate(text(itemNode, "unit"), 32),
                    calories,
                    protein,
                    carbs,
                    fat,
                    fiber));

            totalCalories = totalCalories.add(calories);
            totalProtein = totalProtein.add(protein);
            totalCarbs = totalCarbs.add(carbs);
            totalFat = totalFat.add(fat);
            totalFiber = totalFiber.add(fiber);
        }

        if (totalCalories.compareTo(MAX_PLAUSIBLE_CALORIES) > 0) {
            throw new AiUnavailableException("The analysis returned an implausible calorie total");
        }

        String mealName = text(root, "meal_name");
        if (mealName == null || mealName.isBlank()) {
            mealName = items.get(0).name();
        }

        return new FoodAnalysisResult(
                truncate(mealName, 200),
                items,
                totalCalories,
                totalProtein,
                totalCarbs,
                totalFat,
                totalFiber,
                confidence(root),
                assumptions(root),
                truncate(text(root, "notes"), 1000),
                providerName,
                model,
                rawJson,
                latencyMillis,
                inputTokens,
                outputTokens);
    }

    /**
     * Tolerates a model that wraps its JSON in prose or a fenced code block, which some providers
     * do even when asked not to.
     */
    static String extractJsonObject(String raw) {
        if (raw == null) {
            throw new AiUnavailableException("The analysis service returned an empty response");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int closing = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && closing > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, closing).trim();
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new AiUnavailableException("The analysis service did not return JSON");
        }
        return trimmed.substring(start, end + 1);
    }

    private static BigDecimal macro(JsonNode node, String field, BigDecimal max) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (!value.isNumber()) {
            throw new AiUnavailableException("The analysis returned a non-numeric value for " + field);
        }
        BigDecimal decimal = value.decimalValue();
        if (decimal.signum() < 0) {
            throw new AiUnavailableException("The analysis returned a negative value for " + field);
        }
        if (decimal.compareTo(max) > 0) {
            throw new AiUnavailableException("The analysis returned an implausible value for " + field);
        }
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal optionalNumber(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isNumber() || value.decimalValue().signum() < 0) {
            return null;
        }
        return value.decimalValue().setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal confidence(JsonNode root) {
        JsonNode value = root.path("confidence");
        if (!value.isNumber()) {
            return null;
        }
        BigDecimal confidence = value.decimalValue();
        if (confidence.signum() < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
            return null;
        }
        return confidence.setScale(2, RoundingMode.HALF_UP);
    }

    private static List<String> assumptions(JsonNode root) {
        JsonNode node = root.path("assumptions");
        if (!node.isArray()) {
            return List.of();
        }
        List<String> assumptions = new ArrayList<>();
        for (JsonNode entry : node) {
            if (entry.isTextual() && !entry.asText().isBlank()) {
                assumptions.add(truncate(entry.asText(), 300));
            }
            if (assumptions.size() >= 12) {
                break;
            }
        }
        return List.copyOf(assumptions);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : null;
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
