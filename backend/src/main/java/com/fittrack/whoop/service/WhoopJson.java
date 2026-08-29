package com.fittrack.whoop.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Defensive readers for WHOOP payloads.
 *
 * <p>Every field is optional as far as this code is concerned: a workout may be unscored, a sleep
 * may lack a stage summary, and WHOOP occasionally renames or adds fields. Reading defensively -
 * and keeping the raw payload in JSONB alongside - means a shape change degrades a single column
 * rather than failing the whole synchronisation.
 */
final class WhoopJson {

    private WhoopJson() {}

    static String text(JsonNode node, String... path) {
        JsonNode value = at(node, path);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }

    static Long longValue(JsonNode node, String... path) {
        JsonNode value = at(node, path);
        return value.isNumber() ? value.asLong() : null;
    }

    static Integer intValue(JsonNode node, String... path) {
        JsonNode value = at(node, path);
        if (value.isNumber()) {
            return value.asInt();
        }
        return null;
    }

    /** Rounds a fractional value to the nearest integer, which WHOOP sometimes returns for scores. */
    static Integer roundedInt(JsonNode node, String... path) {
        JsonNode value = at(node, path);
        if (!value.isNumber()) {
            return null;
        }
        return (int) Math.round(value.asDouble());
    }

    static BigDecimal decimal(JsonNode node, int scale, String... path) {
        JsonNode value = at(node, path);
        if (!value.isNumber()) {
            return null;
        }
        return value.decimalValue().setScale(scale, java.math.RoundingMode.HALF_UP);
    }

    static Boolean bool(JsonNode node, String... path) {
        JsonNode value = at(node, path);
        return value.isBoolean() ? value.asBoolean() : null;
    }

    static Instant instant(JsonNode node, String... path) {
        String value = text(node, path);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            try {
                return java.time.OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static JsonNode at(JsonNode node, String... path) {
        JsonNode current = node;
        for (String segment : path) {
            if (current == null) {
                return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
            }
            current = current.path(segment);
        }
        return current == null ? com.fasterxml.jackson.databind.node.MissingNode.getInstance() : current;
    }
}
