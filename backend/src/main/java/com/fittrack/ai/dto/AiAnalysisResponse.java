package com.fittrack.ai.dto;

import com.fittrack.nutrition.dto.FoodItemDto;
import com.fittrack.nutrition.dto.MacrosDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * An AI proposal returned to the client for review.
 *
 * <p>This is explicitly <em>not</em> a nutrition entry. The client shows it on an editable review
 * screen; only a subsequent confirm call creates a food entry.
 */
public record AiAnalysisResponse(
        UUID id,
        String kind,
        String status,
        String provider,
        String model,
        String suggestedName,
        List<FoodItemDto> items,
        MacrosDto totals,
        BigDecimal confidence,
        List<String> assumptions,
        String notes,
        String imageUrl,
        Long latencyMillis,
        UUID confirmedFoodEntryId,
        Instant createdAt) {}
