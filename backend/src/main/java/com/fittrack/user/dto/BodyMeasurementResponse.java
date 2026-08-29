package com.fittrack.user.dto;

import com.fittrack.user.domain.BodyMeasurement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BodyMeasurementResponse(
        UUID id,
        Instant recordedAt,
        BigDecimal weightKg,
        BigDecimal bodyFatPercentage,
        String source,
        String note) {

    public static BodyMeasurementResponse from(BodyMeasurement measurement) {
        return new BodyMeasurementResponse(
                measurement.getId(),
                measurement.getRecordedAt(),
                measurement.getWeightKg(),
                measurement.getBodyFatPercentage(),
                measurement.getSource().name(),
                measurement.getNote());
    }
}
