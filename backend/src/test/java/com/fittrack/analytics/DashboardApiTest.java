package com.fittrack.analytics;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.IntegrationTest;
import com.fittrack.support.TestSupportConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** The dashboard is the most visible surface, so its arithmetic is pinned end to end. */
@IntegrationTest
@Import(TestSupportConfiguration.class)
class DashboardApiTest {

    private static final String DAY = "2026-03-10";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    private static Map<String, Object> macros(double kcal, double p, double c, double f, double fib) {
        return Map.of("calories", kcal, "proteinG", p, "carbsG", c, "fatG", f, "fiberG", fib);
    }

    private void logFood(ApiTestClient.Session session, String name, String at, Map<String, Object> macros)
            throws Exception {
        mockMvc.perform(post("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name, "mealType", "LUNCH", "consumedAt", at, "macros", macros))))
                .andExpect(status().isCreated());
    }

    /**
     * Registration provisions a goal effective from the current date, and goals are resolved as
     * "the most recent row not starting after the day being viewed". So a goal has to be written
     * for the historical day under test <em>and</em> for today, or one of the two reads would fall
     * back to the provisioned defaults.
     */
    private void setGoals(ApiTestClient.Session session) throws Exception {
        upsertGoal(session, "2026-01-01");
        upsertGoal(session, null);
    }

    private void upsertGoal(ApiTestClient.Session session, String effectiveFrom) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "calorieTarget", 2400,
                "proteinTargetG", 160,
                "carbsTargetG", 260,
                "fatTargetG", 80,
                "fiberTargetG", 30,
                "targetBodyWeightKg", 78.0));
        if (effectiveFrom != null) {
            body.put("effectiveFrom", effectiveFrom);
        }
        mockMvc.perform(put("/api/users/me/goals")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void computesConsumedRemainingAndPercentOfTargetForEveryMacro() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);

        logFood(session, "Breakfast", "2026-03-10T07:30:00Z", macros(620, 45, 70, 18, 8));
        logFood(session, "Lunch", "2026-03-10T12:30:00Z", macros(760, 55, 80, 22, 7));
        logFood(session, "Snack", "2026-03-10T16:00:00Z", macros(600, 25, 65, 20, 5));

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", DAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(DAY))
                .andExpect(jsonPath("$.nutrition.entryCount").value(3))
                .andExpect(jsonPath("$.nutrition.calories.consumed").value(closeTo(1980, 0.01)))
                .andExpect(jsonPath("$.nutrition.calories.target").value(closeTo(2400, 0.01)))
                .andExpect(jsonPath("$.nutrition.calories.remaining").value(closeTo(420, 0.01)))
                .andExpect(jsonPath("$.nutrition.calories.percentOfTarget").value(83))
                .andExpect(jsonPath("$.nutrition.protein.consumed").value(closeTo(125, 0.01)))
                .andExpect(jsonPath("$.nutrition.protein.remaining").value(closeTo(35, 0.01)))
                .andExpect(jsonPath("$.nutrition.carbs.consumed").value(closeTo(215, 0.01)))
                .andExpect(jsonPath("$.nutrition.fat.consumed").value(closeTo(60, 0.01)))
                .andExpect(jsonPath("$.nutrition.fiber.consumed").value(closeTo(20, 0.01)))
                .andExpect(jsonPath("$.insights[*].text").value(hasItem("420 kcal remaining")))
                .andExpect(jsonPath("$.insights[*].text").value(hasItem("35 g protein remaining")));
    }

    @Test
    void reportsZeroesRatherThanFailingOnADayWithNothingLogged() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", "2026-03-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nutrition.entryCount").value(0))
                .andExpect(jsonPath("$.nutrition.calories.consumed").value(closeTo(0, 0.01)))
                .andExpect(jsonPath("$.nutrition.calories.remaining").value(closeTo(2400, 0.01)))
                .andExpect(jsonPath("$.weight.latestKg").doesNotExist())
                .andExpect(jsonPath("$.wearableConnected").value(false))
                .andExpect(jsonPath("$.workoutsToday").isEmpty());
    }

    @Test
    void reportsBmrTdeeAndTheBasisUsedForTheEnergyBalance() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);

        mockMvc.perform(put("/api/users/me/settings")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sex", "MALE",
                                "heightCm", 180.0,
                                "birthDate", "1996-01-01",
                                "activityLevel", "MODERATE"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/body-measurements")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("weightKg", 80.0, "recordedAt", "2026-03-10T06:00:00Z"))))
                .andExpect(status().isCreated());

        logFood(session, "All day", "2026-03-10T12:00:00Z", macros(2000, 150, 200, 60, 25));

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", DAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energy.intakeKcal").value(2000))
                .andExpect(jsonPath("$.energy.estimatedBmrKcal").value(1780))
                .andExpect(jsonPath("$.energy.estimatedTdeeKcal").value(2759))
                .andExpect(jsonPath("$.energy.balanceKcal").value(-759))
                .andExpect(jsonPath("$.energy.balanceBasis").value("ESTIMATED_TDEE"))
                // No wearable is connected, so no measured expenditure is claimed.
                .andExpect(jsonPath("$.energy.wearableExpenditureKcal").doesNotExist())
                .andExpect(jsonPath("$.energy.weightKg").value(closeTo(80.0, 0.01)));
    }

    @Test
    void omitsBmrAndTdeeWhenTheInputsAreUnknown() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);
        logFood(session, "Lunch", "2026-03-10T12:00:00Z", macros(800, 60, 80, 25, 10));

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", DAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energy.estimatedBmrKcal").doesNotExist())
                .andExpect(jsonPath("$.energy.estimatedTdeeKcal").doesNotExist())
                .andExpect(jsonPath("$.energy.balanceBasis").value("NONE"))
                .andExpect(jsonPath("$.energy.balanceKcal").doesNotExist());
    }

    @Test
    void weightTrendComparesRollingAveragesRatherThanSingleReadings() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);

        // Previous week averages 82.4; current week averages 82.0 -> a 0.4 kg decrease.
        record Reading(String at, double kg) {}
        for (Reading reading : new Reading[] {
            new Reading("2026-03-01T06:00:00Z", 82.5),
            new Reading("2026-03-02T06:00:00Z", 82.4),
            new Reading("2026-03-03T06:00:00Z", 82.3),
            new Reading("2026-03-05T06:00:00Z", 82.1),
            new Reading("2026-03-07T06:00:00Z", 82.0),
            new Reading("2026-03-09T06:00:00Z", 81.9)
        }) {
            mockMvc.perform(post("/api/body-measurements")
                            .header(HttpHeaders.AUTHORIZATION, session.bearer())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("weightKg", reading.kg(), "recordedAt", reading.at()))))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/body-measurements/trend")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestKg").value(closeTo(81.9, 0.01)))
                .andExpect(jsonPath("$.targetKg").value(closeTo(78.0, 0.01)))
                .andExpect(jsonPath("$.toTargetKg").value(closeTo(3.9, 0.01)));
    }

    @Test
    void theDashboardIsScopedToTheCaller() throws Exception {
        ApiTestClient.Session alice = apiTestClient.registerRandomUser();
        ApiTestClient.Session bob = apiTestClient.registerRandomUser();
        setGoals(alice);
        setGoals(bob);

        logFood(alice, "Alice's lunch", "2026-03-10T12:00:00Z", macros(900, 70, 90, 30, 12));

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, bob.bearer())
                        .param("date", DAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nutrition.entryCount").value(0))
                .andExpect(jsonPath("$.nutrition.calories.consumed").value(closeTo(0, 0.01)));
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    void analyticsSeriesFillsEveryDayInTheRangeAndAggregatesInTheBackend() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        setGoals(session);

        logFood(session, "Day 1", "2026-03-01T12:00:00Z", macros(2000, 150, 200, 60, 25));
        logFood(session, "Day 3", "2026-03-03T12:00:00Z", macros(2400, 170, 240, 70, 30));

        mockMvc.perform(get("/api/analytics/series")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("from", "2026-03-01")
                        .param("to", "2026-03-05")
                        .param("granularity", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularity").value("DAY"))
                .andExpect(jsonPath("$.buckets.length()").value(5))
                .andExpect(jsonPath("$.buckets[0].date").value("2026-03-01"))
                .andExpect(jsonPath("$.buckets[0].calories").value(closeTo(2000, 0.01)))
                // A day with nothing logged is present with no calorie figure, not missing.
                .andExpect(jsonPath("$.buckets[1].calories").doesNotExist())
                .andExpect(jsonPath("$.buckets[2].calories").value(closeTo(2400, 0.01)))
                // Averages are over days that actually have entries.
                .andExpect(jsonPath("$.totals.daysLogged").value(2))
                .andExpect(jsonPath("$.totals.averageCalories").value(closeTo(2200, 0.01)));
    }

    @Test
    void analyticsRejectsAnInvertedOrExcessiveRange() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/analytics/series")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("from", "2026-03-10")
                        .param("to", "2026-03-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        mockMvc.perform(get("/api/analytics/series")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("from", "2020-01-01")
                        .param("to", "2026-03-01"))
                .andExpect(status().isBadRequest());
    }
}
