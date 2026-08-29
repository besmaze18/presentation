package com.fittrack.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.provider.FoodAnalysisResponseParser;
import com.fittrack.ai.service.AiUnavailableException;
import org.junit.jupiter.api.Test;

/**
 * The parser is the boundary between an untrusted model response and the database, so it rejects
 * anything implausible outright rather than quietly repairing it.
 */
class FoodAnalysisResponseParserTest {

    private final FoodAnalysisResponseParser parser =
            new FoodAnalysisResponseParser(new ObjectMapper());

    private FoodAnalysisResult parse(String json) {
        return parser.parse(json, "stub", "stub-model", 10L, 100L, 20L);
    }

    @Test
    void parsesAWellFormedResponseAndSumsTheItemTotals() {
        FoodAnalysisResult result = parse(
                """
                {"meal_name":"Breakfast",
                 "items":[
                   {"name":"Oats","quantity":80,"unit":"g","calories":296,"protein_g":10.4,
                    "carbs_g":48,"fat_g":5.6,"fiber_g":8},
                   {"name":"Whole milk","quantity":200,"unit":"ml","calories":122,"protein_g":6.6,
                    "carbs_g":9.6,"fat_g":6.6,"fiber_g":0}],
                 "confidence":0.82,
                 "assumptions":["Milk assumed whole"],
                 "notes":"Weighed dry"}
                """);

        assertThat(result.mealName()).isEqualTo("Breakfast");
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalCalories()).isEqualByComparingTo("418.00");
        assertThat(result.totalProteinG()).isEqualByComparingTo("17.00");
        assertThat(result.totalFiberG()).isEqualByComparingTo("8.00");
        assertThat(result.confidence()).isEqualByComparingTo("0.82");
        assertThat(result.assumptions()).containsExactly("Milk assumed whole");
        assertThat(result.providerName()).isEqualTo("stub");
        assertThat(result.latencyMillis()).isEqualTo(10L);
    }

    @Test
    void unwrapsJsonThatArrivedInsideAFencedCodeBlock() {
        FoodAnalysisResult result = parse(
                """
                Here you go:
                ```json
                {"meal_name":"Snack","items":[{"name":"Apple","quantity":1,"unit":"piece",
                 "calories":95,"protein_g":0.5,"carbs_g":25,"fat_g":0.3,"fiber_g":4.4}],
                 "confidence":0.9,"assumptions":[]}
                ```
                """);

        assertThat(result.mealName()).isEqualTo("Snack");
        assertThat(result.totalCalories()).isEqualByComparingTo("95.00");
    }

    @Test
    void rejectsAResponseWithNoIdentifiedFood() {
        assertThatThrownBy(() -> parse("{\"meal_name\":\"Nothing\",\"items\":[],\"confidence\":0.1}"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("No food could be identified");
    }

    @Test
    void rejectsNegativeAndNonNumericMacros() {
        assertThatThrownBy(() -> parse(
                        """
                        {"meal_name":"Bad","items":[{"name":"X","quantity":1,"unit":"g",
                         "calories":-5,"protein_g":1,"carbs_g":1,"fat_g":1,"fiber_g":1}],
                         "confidence":0.5,"assumptions":[]}
                        """))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("negative value");

        assertThatThrownBy(() -> parse(
                        """
                        {"meal_name":"Bad","items":[{"name":"X","quantity":1,"unit":"g",
                         "calories":"lots","protein_g":1,"carbs_g":1,"fat_g":1,"fiber_g":1}],
                         "confidence":0.5,"assumptions":[]}
                        """))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("non-numeric");
    }

    @Test
    void rejectsImplausibleQuantities() {
        assertThatThrownBy(() -> parse(
                        """
                        {"meal_name":"Bad","items":[{"name":"X","quantity":1,"unit":"g",
                         "calories":9999999,"protein_g":1,"carbs_g":1,"fat_g":1,"fiber_g":1}],
                         "confidence":0.5,"assumptions":[]}
                        """))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("implausible");
    }

    @Test
    void rejectsAnUnnamedItem() {
        assertThatThrownBy(() -> parse(
                        """
                        {"meal_name":"Bad","items":[{"quantity":1,"unit":"g","calories":10,
                         "protein_g":1,"carbs_g":1,"fat_g":1,"fiber_g":1}],
                         "confidence":0.5,"assumptions":[]}
                        """))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("unnamed");
    }

    @Test
    void rejectsSomethingThatIsNotJsonAtAll() {
        assertThatThrownBy(() -> parse("I'm sorry, I can't help with that."))
                .isInstanceOf(AiUnavailableException.class);
        assertThatThrownBy(() -> parse(null)).isInstanceOf(AiUnavailableException.class);
    }

    @Test
    void treatsAnOutOfRangeConfidenceAsUnknownRatherThanTrustingIt() {
        FoodAnalysisResult result = parse(
                """
                {"meal_name":"Snack","items":[{"name":"Apple","quantity":1,"unit":"piece",
                 "calories":95,"protein_g":0.5,"carbs_g":25,"fat_g":0.3,"fiber_g":4.4}],
                 "confidence":7,"assumptions":[]}
                """);

        assertThat(result.confidence()).isNull();
    }

    @Test
    void missingMacroFieldsDefaultToZeroRatherThanFailing() {
        FoodAnalysisResult result = parse(
                """
                {"meal_name":"Coffee","items":[{"name":"Black coffee","quantity":250,"unit":"ml",
                 "calories":2}],"confidence":0.95,"assumptions":[]}
                """);

        assertThat(result.totalCalories()).isEqualByComparingTo("2.00");
        assertThat(result.totalProteinG()).isEqualByComparingTo("0.00");
    }
}
