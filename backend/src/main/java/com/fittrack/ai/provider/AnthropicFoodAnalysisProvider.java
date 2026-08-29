package com.fittrack.ai.provider;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.dto.ImageAnalysisRequest;
import com.fittrack.ai.dto.NutritionEstimateRequest;
import com.fittrack.ai.dto.TextAnalysisRequest;
import com.fittrack.ai.service.AiUnavailableException;
import com.fittrack.ai.service.FoodAnalysisService;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Anthropic implementation of {@link FoodAnalysisService}.
 *
 * <p>This is the only class in the application that knows which model provider is in use.
 * Substituting a different vendor means adding a sibling class here and changing
 * {@code AI_PROVIDER} - no domain, controller or persistence code changes.
 */
public class AnthropicFoodAnalysisProvider implements FoodAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicFoodAnalysisProvider.class);
    private static final String PROVIDER = "anthropic";

    private static final String SYSTEM_PROMPT =
            """
            You are a nutrition estimation assistant inside a food-tracking application.

            Estimate the nutritional content of the food described or shown. Break the meal into \
            individual food items and give calories, protein, carbohydrates, fat and fiber for each \
            item at the portion you estimate.

            Rules:
            - Use grams for solid foods and millilitres for liquids unless a count is more natural \
              (piece, slice, egg).
            - Base estimates on standard reference nutrition data for the food as prepared.
            - Include cooking fats and sauces you can reasonably infer, and list every such \
              inference in "assumptions".
            - Set "confidence" to your genuine self-assessed confidence between 0 and 1. Use a low \
              value when the portion size is hard to judge from the information given.
            - Never invent a food you cannot see or that was not described.
            - Reply with JSON only, matching the provided schema exactly. No prose, no code fences.

            Your output is a proposal that the user reviews and corrects before it is saved, so \
            honest uncertainty is more useful than false precision.
            """;

    private final AnthropicClient client;
    private final AiProperties.Anthropic config;
    private final FoodAnalysisResponseParser parser;
    private final ObjectMapper objectMapper;
    private final JsonNode schema;

    public AnthropicFoodAnalysisProvider(
            AnthropicClient client,
            AiProperties.Anthropic config,
            FoodAnalysisResponseParser parser,
            ObjectMapper objectMapper) {
        this.client = client;
        this.config = config;
        this.parser = parser;
        this.objectMapper = objectMapper;
        this.schema = FoodAnalysisSchema.build(objectMapper);
    }

    @Override
    public String providerName() {
        return PROVIDER;
    }

    @Override
    public boolean isAvailable() {
        return client != null && config.isConfigured();
    }

    @Override
    public FoodAnalysisResult analyzeText(TextAnalysisRequest request) {
        if (request.description() == null || request.description().isBlank()) {
            throw new AiUnavailableException("Nothing to analyse");
        }
        String prompt =
                "Estimate the nutrition for the following meal description.\n\n"
                        + request.description().trim()
                        + localeSuffix(request.localeHint());
        return send(List.of(ContentBlockParam.ofText(TextBlockParam.builder().text(prompt).build())));
    }

    @Override
    public FoodAnalysisResult analyzeImage(ImageAnalysisRequest request) {
        if (request.image() == null || request.image().length == 0) {
            throw new AiUnavailableException("Nothing to analyse");
        }
        ImageBlockParam image = ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .mediaType(mediaTypeOf(request.contentType()))
                        .data(Base64.getEncoder().encodeToString(request.image()))
                        .build())
                .build();

        StringBuilder prompt = new StringBuilder(
                "Identify every food in this photograph and estimate the portion and nutrition "
                        + "of each. Judge portions against any visible reference such as cutlery, "
                        + "a plate rim or a hand.");
        if (request.userHint() != null && !request.userHint().isBlank()) {
            prompt.append("\n\nThe user adds: ").append(request.userHint().trim());
        }
        prompt.append(localeSuffix(request.localeHint()));

        return send(List.of(
                ContentBlockParam.ofImage(image),
                ContentBlockParam.ofText(TextBlockParam.builder().text(prompt.toString()).build())));
    }

    @Override
    public FoodAnalysisResult estimateNutrition(NutritionEstimateRequest request) {
        if (request.foodName() == null || request.foodName().isBlank()) {
            throw new AiUnavailableException("Nothing to analyse");
        }
        String quantity = request.quantity() == null
                ? "one typical serving"
                : request.quantity().toPlainString() + " " + (request.unit() == null ? "g" : request.unit());
        String prompt = "Estimate the nutrition for " + quantity + " of " + request.foodName().trim() + ".";
        return send(List.of(ContentBlockParam.ofText(TextBlockParam.builder().text(prompt).build())));
    }

    private FoodAnalysisResult send(List<ContentBlockParam> content) {
        if (!isAvailable()) {
            throw new AiUnavailableException("AI analysis is not configured on this instance");
        }

        MessageCreateParams params = MessageCreateParams.builder()
                .model(config.getModel())
                .maxTokens(config.getMaxTokens())
                .system(SYSTEM_PROMPT)
                // Structured output: the provider constrains the response to our schema, so the
                // parser is a second line of defence rather than the only one.
                .outputConfig(OutputConfig.builder()
                        .format(JsonOutputFormat.builder()
                                .schema(JsonOutputFormat.Schema.builder()
                                        .putAllAdditionalProperties(schemaProperties())
                                        .build())
                                .build())
                        .build())
                .addUserMessageOfBlockParams(content)
                .build();

        long startedAt = System.nanoTime();
        Message response;
        try {
            response = client.messages().create(params);
        } catch (AnthropicServiceException ex) {
            // Never log the request content or the API key - only the provider's error class.
            log.warn("Anthropic request failed: {}", ex.getClass().getSimpleName());
            throw new AiUnavailableException("The analysis service is temporarily unavailable", ex);
        } catch (RuntimeException ex) {
            log.warn("Anthropic request failed: {}", ex.getClass().getSimpleName());
            throw new AiUnavailableException("The analysis service could not be reached", ex);
        }
        long latencyMillis = (System.nanoTime() - startedAt) / 1_000_000;

        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(com.anthropic.models.messages.TextBlock::text)
                .reduce("", String::concat);

        if (text.isBlank()) {
            throw new AiUnavailableException("The analysis service returned no content");
        }

        return parser.parse(
                text,
                PROVIDER,
                config.getModel(),
                latencyMillis,
                response.usage().inputTokens(),
                response.usage().outputTokens());
    }

    /** Converts the schema document into the map shape the SDK's schema builder accepts. */
    private Map<String, JsonValue> schemaProperties() {
        Map<String, Object> asMap = objectMapper.convertValue(schema, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        return asMap.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey, entry -> JsonValue.from(entry.getValue())));
    }

    private static Base64ImageSource.MediaType mediaTypeOf(String contentType) {
        String normalised = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        return switch (normalised) {
            case "image/png" -> Base64ImageSource.MediaType.IMAGE_PNG;
            case "image/gif" -> Base64ImageSource.MediaType.IMAGE_GIF;
            case "image/webp" -> Base64ImageSource.MediaType.IMAGE_WEBP;
            case "image/jpeg" -> Base64ImageSource.MediaType.IMAGE_JPEG;
            default -> throw new AiUnavailableException("Unsupported image type for analysis");
        };
    }

    private static String localeSuffix(String localeHint) {
        return localeHint == null || localeHint.isBlank()
                ? ""
                : "\n\nThe user is in " + localeHint + "; prefer portion conventions common there.";
    }
}
