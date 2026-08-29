package com.fittrack.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyzeTextRequest(
        @NotBlank @Size(max = 2000) String description, @Size(max = 64) String localeHint) {}
