package com.fittrack.analytics.dto;

/**
 * A calculated observation. Every one of these is derived deterministically from stored data -
 * no language model produces the numbers. {@code code} lets a future conversational layer explain
 * an insight without re-deriving it.
 */
public record InsightDto(String code, String text, Tone tone) {

    public enum Tone {
        NEUTRAL,
        POSITIVE,
        WARNING
    }

    public static InsightDto neutral(String code, String text) {
        return new InsightDto(code, text, Tone.NEUTRAL);
    }

    public static InsightDto positive(String code, String text) {
        return new InsightDto(code, text, Tone.POSITIVE);
    }

    public static InsightDto warning(String code, String text) {
        return new InsightDto(code, text, Tone.WARNING);
    }
}
