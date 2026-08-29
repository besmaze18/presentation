package com.fittrack.analytics.dto;

import java.math.BigDecimal;

/** One macro line on the dashboard: what was eaten, the target, and what remains. */
public record MacroProgress(
        BigDecimal consumed, BigDecimal target, BigDecimal remaining, Integer percentOfTarget) {}
