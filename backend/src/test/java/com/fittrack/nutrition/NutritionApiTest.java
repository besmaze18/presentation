package com.fittrack.nutrition;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.IntegrationTest;
import com.fittrack.support.TestSupportConfiguration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@IntegrationTest
@Import(TestSupportConfiguration.class)
class NutritionApiTest {

    private static final String DAY = "2026-03-10";
    private static final String NOON = "2026-03-10T12:00:00Z";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    private String createEntry(ApiTestClient.Session session, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    private static Map<String, Object> macros(double kcal, double p, double c, double f, double fib) {
        return Map.of("calories", kcal, "proteinG", p, "carbsG", c, "fatG", f, "fiberG", fib);
    }

    @Test
    void createsAManualEntryAndReturnsItInTheDayView() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        String id = createEntry(
                session,
                Map.of(
                        "name", "Chicken and rice",
                        "mealType", "LUNCH",
                        "consumedAt", NOON,
                        "quantity", 1,
                        "unit", "plate",
                        "macros", macros(650, 55, 70, 14, 4)));

        mockMvc.perform(get("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chicken and rice"))
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.source").value("MANUAL"))
                .andExpect(jsonPath("$.entryDate").value(DAY))
                .andExpect(jsonPath("$.macros.calories").value(closeTo(650, 0.01)));

        mockMvc.perform(get("/api/nutrition/days/" + DAY)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryCount").value(1))
                .andExpect(jsonPath("$.totals.calories").value(closeTo(650, 0.01)))
                .andExpect(jsonPath("$.totals.proteinG").value(closeTo(55, 0.01)))
                .andExpect(jsonPath("$.entries[0].id").value(id));
    }

    @Test
    void sumsMultipleEntriesForTheSameDay() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        createEntry(session, Map.of(
                "name", "Breakfast", "mealType", "BREAKFAST", "consumedAt", "2026-03-10T07:30:00Z",
                "macros", macros(420, 32, 45, 12, 6)));
        createEntry(session, Map.of(
                "name", "Lunch", "mealType", "LUNCH", "consumedAt", NOON,
                "macros", macros(650, 55, 70, 14, 4)));

        mockMvc.perform(get("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", DAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryCount").value(2))
                .andExpect(jsonPath("$.totals.calories").value(closeTo(1070, 0.01)))
                .andExpect(jsonPath("$.totals.proteinG").value(closeTo(87, 0.01)))
                .andExpect(jsonPath("$.totals.fiberG").value(closeTo(10, 0.01)));
    }

    @Test
    void derivesEntryTotalsFromItemsWhenItemsAreSupplied() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        String id = createEntry(
                session,
                Map.of(
                        "name", "Post-training meal",
                        "mealType", "DINNER",
                        "consumedAt", NOON,
                        // The supplied top-level macros are deliberately wrong: items win.
                        "macros", macros(1, 1, 1, 1, 1),
                        "items", List.of(
                                Map.of("name", "Chicken breast", "quantity", 300, "unit", "g",
                                        "macros", macros(495, 93, 0, 11, 0)),
                                Map.of("name", "Basmati rice, cooked", "quantity", 200, "unit", "g",
                                        "macros", macros(260, 5.2, 56, 0.6, 0.8)),
                                Map.of("name", "Olive oil", "quantity", 15, "unit", "g",
                                        "macros", macros(133, 0, 0, 15, 0)))));

        mockMvc.perform(get("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3))
                .andExpect(jsonPath("$.macros.calories").value(closeTo(888, 0.01)))
                .andExpect(jsonPath("$.macros.proteinG").value(closeTo(98.2, 0.01)))
                .andExpect(jsonPath("$.macros.carbsG").value(closeTo(56, 0.01)))
                .andExpect(jsonPath("$.macros.fatG").value(closeTo(26.6, 0.01)));
    }

    @Test
    void updatesAnEntryAndMovesItToTheNewDayWhenTheTimeChanges() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        String id = createEntry(session, Map.of(
                "name", "Snack", "mealType", "SNACK", "consumedAt", NOON, "macros", macros(200, 10, 20, 8, 2)));

        mockMvc.perform(put("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Greek yoghurt",
                                "mealType", "BREAKFAST",
                                "consumedAt", "2026-03-11T08:00:00Z",
                                "macros", macros(180, 18, 12, 6, 0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Greek yoghurt"))
                .andExpect(jsonPath("$.entryDate").value("2026-03-11"));

        mockMvc.perform(get("/api/nutrition/days/" + DAY)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.entryCount").value(0));

        mockMvc.perform(get("/api/nutrition/days/2026-03-11")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.entryCount").value(1));
    }

    @Test
    void deletesAnEntry() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        String id = createEntry(session, Map.of(
                "name", "Snack", "mealType", "SNACK", "consumedAt", NOON, "macros", macros(200, 10, 20, 8, 2)));

        mockMvc.perform(delete("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsAnEntryWithNeitherMacrosNorItems() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(post("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Mystery meal", "mealType", "LUNCH", "consumedAt", NOON))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void rejectsNegativeMacros() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(post("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Impossible", "mealType", "LUNCH", "consumedAt", NOON,
                                "macros", macros(-10, 0, 0, 0, 0)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("macros.calories"));
    }

    @Test
    void oneUserCannotReadEditOrDeleteAnotherUsersEntry() throws Exception {
        ApiTestClient.Session owner = apiTestClient.registerRandomUser();
        ApiTestClient.Session intruder = apiTestClient.registerRandomUser();

        String id = createEntry(owner, Map.of(
                "name", "Private meal", "mealType", "DINNER", "consumedAt", NOON,
                "macros", macros(700, 40, 60, 25, 8)));

        mockMvc.perform(get("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Hijacked", "mealType", "DINNER", "consumedAt", NOON,
                                "macros", macros(0, 0, 0, 0, 0)))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isNotFound());

        // The owner's entry is untouched.
        mockMvc.perform(get("/api/nutrition/entries/" + id)
                        .header(HttpHeaders.AUTHORIZATION, owner.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Private meal"));
    }

    @Test
    void dayViewsAreScopedToTheCaller() throws Exception {
        ApiTestClient.Session alice = apiTestClient.registerRandomUser();
        ApiTestClient.Session bob = apiTestClient.registerRandomUser();

        createEntry(alice, Map.of(
                "name", "Alice lunch", "mealType", "LUNCH", "consumedAt", NOON,
                "macros", macros(600, 40, 60, 20, 5)));

        mockMvc.perform(get("/api/nutrition/days/" + DAY)
                        .header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryCount").value(0))
                .andExpect(jsonPath("$.totals.calories").value(closeTo(0, 0.001)));
    }

    @Test
    void unknownEntryIdReturnsNotFound() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/nutrition/entries/" + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void savedFoodsCanBeCreatedSearchedAndLoggedAtAScaledQuantity() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        MvcResult created = mockMvc.perform(post("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Chicken breast",
                                "servingQuantity", 100,
                                "servingUnit", "g",
                                "macros", macros(165, 31, 0, 3.6, 0),
                                "defaultMealType", "LUNCH"))))
                .andExpect(status().isCreated())
                .andReturn();
        String foodId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(get("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("query", "chick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Chicken breast"));

        // 250 g is 2.5 servings: every macro scales by the same factor.
        mockMvc.perform(post("/api/foods/" + foodId + "/log")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("quantity", 250, "consumedAt", NOON))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("SAVED_FOOD"))
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.unit").value("g"))
                .andExpect(jsonPath("$.macros.calories").value(closeTo(412.5, 0.01)))
                .andExpect(jsonPath("$.macros.proteinG").value(closeTo(77.5, 0.01)))
                .andExpect(jsonPath("$.macros.fatG").value(closeTo(9, 0.01)));

        MvcResult afterUse = mockMvc.perform(get("/api/foods/" + foodId)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode food = objectMapper.readTree(afterUse.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(food.path("usageCount").asLong()).isEqualTo(1);
    }

    @Test
    void savedFoodNamesAreUniquePerUserButNotAcrossUsers() throws Exception {
        ApiTestClient.Session alice = apiTestClient.registerRandomUser();
        ApiTestClient.Session bob = apiTestClient.registerRandomUser();
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Oats",
                "servingQuantity", 100,
                "servingUnit", "g",
                "macros", macros(370, 13, 60, 7, 10)));

        mockMvc.perform(post("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, alice.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, alice.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        // Bob is a separate namespace.
        mockMvc.perform(post("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, bob.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/foods").header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void savedFoodsAreNotVisibleOrLoggableAcrossUsers() throws Exception {
        ApiTestClient.Session owner = apiTestClient.registerRandomUser();
        ApiTestClient.Session intruder = apiTestClient.registerRandomUser();

        MvcResult created = mockMvc.perform(post("/api/foods")
                        .header(HttpHeaders.AUTHORIZATION, owner.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Protein shake",
                                "servingQuantity", 1,
                                "servingUnit", "scoop",
                                "macros", macros(120, 24, 3, 1.5, 0)))))
                .andExpect(status().isCreated())
                .andReturn();
        String foodId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(get("/api/foods/" + foodId)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/foods/" + foodId + "/log")
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 1))))
                .andExpect(status().isNotFound());
    }
}
