package com.fittrack.nutrition.domain;

/**
 * How a logged meal came to exist. Recording this alongside the AI's original prediction is what
 * makes it possible to measure estimation accuracy later.
 */
public enum FoodEntrySource {
    MANUAL,
    SAVED_FOOD,
    AI_TEXT,
    AI_IMAGE
}
