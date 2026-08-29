package com.fittrack.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The JSON contract the model must produce.
 *
 * <p>It is expressed as a plain JSON Schema document rather than a vendor type, so the same
 * definition can be handed to any provider that supports structured output, and used as a
 * validation schema for providers that do not.
 */
final class FoodAnalysisSchema {

    private FoodAnalysisSchema() {}

    static JsonNode build(ObjectMapper mapper) {
        ObjectNode item = mapper.createObjectNode();
        item.put("type", "object");
        ObjectNode itemProperties = item.putObject("properties");
        putString(itemProperties, "name", "Food name in English, e.g. 'Grilled chicken breast'");
        putNumber(itemProperties, "quantity", "Estimated amount of this item");
        putString(itemProperties, "unit", "Unit for the quantity, e.g. g, ml, piece, slice");
        putNumber(itemProperties, "calories", "Kilocalories for this item at the stated quantity");
        putNumber(itemProperties, "protein_g", "Protein in grams");
        putNumber(itemProperties, "carbs_g", "Carbohydrates in grams");
        putNumber(itemProperties, "fat_g", "Fat in grams");
        putNumber(itemProperties, "fiber_g", "Fiber in grams");
        item.set(
                "required",
                mapper.createArrayNode()
                        .add("name")
                        .add("quantity")
                        .add("unit")
                        .add("calories")
                        .add("protein_g")
                        .add("carbs_g")
                        .add("fat_g")
                        .add("fiber_g"));
        item.put("additionalProperties", false);

        ObjectNode root = mapper.createObjectNode();
        root.put("type", "object");
        ObjectNode properties = root.putObject("properties");
        putString(properties, "meal_name", "A short name for the whole meal");
        ObjectNode items = properties.putObject("items");
        items.put("type", "array");
        items.put("description", "One entry per distinct food detected");
        items.set("items", item);
        putNumber(properties, "confidence", "Self-assessed confidence between 0 and 1");
        ObjectNode assumptions = properties.putObject("assumptions");
        assumptions.put("type", "array");
        assumptions.put(
                "description",
                "Every guess made: cooking method, hidden oil or sauces, portion size, brand");
        assumptions.putObject("items").put("type", "string");
        putString(properties, "notes", "Anything the user should check before confirming");

        root.set(
                "required",
                mapper.createArrayNode().add("meal_name").add("items").add("confidence").add("assumptions"));
        root.put("additionalProperties", false);
        return root;
    }

    private static void putString(ObjectNode parent, String name, String description) {
        ObjectNode node = parent.putObject(name);
        node.put("type", "string");
        node.put("description", description);
    }

    private static void putNumber(ObjectNode parent, String name, String description) {
        ObjectNode node = parent.putObject(name);
        node.put("type", "number");
        node.put("description", description);
    }
}
