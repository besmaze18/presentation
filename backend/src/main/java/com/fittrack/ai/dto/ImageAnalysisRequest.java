package com.fittrack.ai.dto;

/** A meal photograph, already validated and stored, with an optional user hint. */
public record ImageAnalysisRequest(
        byte[] image, String contentType, String userHint, String localeHint) {}
