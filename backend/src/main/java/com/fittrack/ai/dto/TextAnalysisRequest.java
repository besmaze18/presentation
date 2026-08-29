package com.fittrack.ai.dto;

/** "300g chicken breast, 200g cooked basmati rice and 15g olive oil". */
public record TextAnalysisRequest(String description, String localeHint) {}
