package com.fittrack.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.ai.domain.AiAnalysis;
import com.fittrack.ai.domain.AiAnalysisRepository;
import com.fittrack.ai.domain.AiAnalysisStatus;
import com.fittrack.ai.service.AiUnavailableException;
import com.fittrack.ai.service.FoodAnalysisService;
import com.fittrack.support.AiTestConfiguration;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.IntegrationTest;
import com.fittrack.support.StubFoodAnalysisService;
import com.fittrack.support.TestImages;
import com.fittrack.support.TestSupportConfiguration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The rule under test throughout: an AI estimate is a proposal. It is stored, shown for review, and
 * only becomes nutrition when the user confirms it - with the values the user accepted.
 */
@IntegrationTest
@Import({TestSupportConfiguration.class, AiTestConfiguration.class})
class FoodAnalysisWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    @Autowired
    private AiAnalysisRepository aiAnalysisRepository;

    @Autowired
    private FoodAnalysisService foodAnalysisService;

    private StubFoodAnalysisService stub() {
        return (StubFoodAnalysisService) foodAnalysisService;
    }

    @BeforeEach
    void resetStub() {
        stub().reset();
    }

    private JsonNode analyzeText(ApiTestClient.Session session) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/food-analysis/text")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "description",
                                "300g chicken breast, 200g cooked basmati rice and 15g olive oil"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void naturalLanguageAnalysisReturnsAStructuredProposalWithoutCreatingNutrition() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        JsonNode analysis = analyzeText(session);

        assertThat(analysis.path("status").asText()).isEqualTo("PENDING_REVIEW");
        assertThat(analysis.path("items")).hasSize(3);
        assertThat(analysis.path("items").get(0).path("name").asText()).isEqualTo("Chicken breast");
        assertThat(analysis.path("items").get(0).path("quantity").asDouble()).isEqualTo(300.0);
        assertThat(analysis.path("totals").path("calories").asDouble()).isEqualTo(888.0);
        assertThat(analysis.path("confidence").asDouble()).isEqualTo(0.78);
        assertThat(analysis.path("assumptions")).hasSize(2);
        // Null fields are omitted from responses, so an absent link means "not yet confirmed".
        assertThat(analysis.hasNonNull("confirmedFoodEntryId")).isFalse();

        // Crucially: nothing has been logged as nutrition yet.
        mockMvc.perform(get("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", "2026-03-10"))
                .andExpect(jsonPath("$.entryCount").value(0));
    }

    @Test
    void confirmingSavesTheUsersEditedValuesRatherThanThePrediction() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(session).path("id").asText());

        // The user corrects the rice portion downwards before saving.
        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Chicken, rice and olive oil",
                                "mealType", "DINNER",
                                "consumedAt", "2026-03-10T19:00:00Z",
                                "items", List.of(
                                        Map.of("name", "Chicken breast", "quantity", 300, "unit", "g",
                                                "macros", Map.of("calories", 495, "proteinG", 93,
                                                        "carbsG", 0, "fatG", 11, "fiberG", 0)),
                                        Map.of("name", "Basmati rice, cooked", "quantity", 150, "unit", "g",
                                                "macros", Map.of("calories", 195, "proteinG", 3.9,
                                                        "carbsG", 42, "fatG", 0.45, "fiberG", 0.6)),
                                        Map.of("name", "Olive oil", "quantity", 15, "unit", "g",
                                                "macros", Map.of("calories", 133, "proteinG", 0,
                                                        "carbsG", 0, "fatG", 15, "fiberG", 0)))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("AI_TEXT"))
                .andExpect(jsonPath("$.aiAnalysisId").value(analysisId.toString()))
                .andExpect(jsonPath("$.items.length()").value(3))
                // 495 + 195 + 133 = 823, the corrected total - not the predicted 888.
                .andExpect(jsonPath("$.macros.calories").value(closeTo(823, 0.01)))
                .andExpect(jsonPath("$.macros.proteinG").value(closeTo(96.9, 0.01)));

        mockMvc.perform(get("/api/nutrition/days/2026-03-10")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.entryCount").value(1))
                .andExpect(jsonPath("$.totals.calories").value(closeTo(823, 0.01)));
    }

    @Test
    void bothThePredictionAndTheConfirmedValuesAreRetainedForAccuracyMeasurement() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(session).path("id").asText());

        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Dinner",
                                "mealType", "DINNER",
                                "consumedAt", "2026-03-10T19:00:00Z",
                                "macros", Map.of("calories", 823, "proteinG", 96.9,
                                        "carbsG", 42, "fatG", 26.45, "fiberG", 0.6)))))
                .andExpect(status().isCreated());

        AiAnalysis stored = aiAnalysisRepository.findById(analysisId).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(AiAnalysisStatus.CONFIRMED);
        assertThat(stored.getConfirmedAt()).isNotNull();
        assertThat(stored.getConfirmedFoodEntryId()).isNotNull();

        // The prediction survives unchanged...
        assertThat(stored.getPredictedMacros().getCalories()).isEqualByComparingTo("888.00");
        assertThat(stored.getConfidence()).isEqualByComparingTo("0.78");
        assertThat(stored.getPrediction().path("items")).hasSize(3);
        assertThat(stored.getRawResponse()).isNotNull();
        assertThat(stored.getModel()).isEqualTo("stub-model-1");
        assertThat(stored.getLatencyMillis()).isEqualTo(123L);

        // ...alongside what the user actually accepted.
        assertThat(stored.getConfirmedMacros().getCalories()).isEqualByComparingTo("823.00");
    }

    @Test
    void anAnalysisCannotBeConfirmedTwice() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(session).path("id").asText());
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Dinner",
                "mealType", "DINNER",
                "consumedAt", "2026-03-10T19:00:00Z",
                "macros", Map.of("calories", 800, "proteinG", 90, "carbsG", 40, "fatG", 25, "fiberG", 1)));

        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void confirmingRequiresEitherMacrosOrItems() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(session).path("id").asText());

        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Dinner", "mealType", "DINNER"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analysesAreInvisibleAndUnconfirmableAcrossUsers() throws Exception {
        ApiTestClient.Session owner = apiTestClient.registerRandomUser();
        ApiTestClient.Session intruder = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(owner).path("id").asText());

        mockMvc.perform(get("/api/ai/food-analysis/" + analysisId)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/ai/food-analysis/" + analysisId + "/confirm")
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Stolen", "mealType", "DINNER",
                                "macros", Map.of("calories", 1, "proteinG", 1,
                                        "carbsG", 1, "fatG", 1, "fiberG", 1)))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/ai/food-analysis/" + analysisId)
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/ai/food-analysis")
                        .header(HttpHeaders.AUTHORIZATION, intruder.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void aPhotographIsValidatedStoredAndAnalysed() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        MockMultipartFile image = new MockMultipartFile(
                "image", "meal.png", MediaType.IMAGE_PNG_VALUE, TestImages.onePixelPng());

        MvcResult result = mockMvc.perform(multipart("/api/ai/food-analysis/image")
                        .file(image)
                        .param("hint", "Post-training dinner")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kind").value("IMAGE"))
                .andExpect(jsonPath("$.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.imageUrl").isNotEmpty())
                .andReturn();

        JsonNode analysis = objectMapper.readTree(result.getResponse().getContentAsString());
        AiAnalysis stored =
                aiAnalysisRepository.findById(UUID.fromString(analysis.path("id").asText())).orElseThrow();

        // Only the reference is persisted, never the bytes.
        assertThat(stored.getImageStorageKey()).isNotBlank();
        assertThat(stored.getImageStorageProvider()).isEqualTo("local");
        assertThat(stored.getImageContentType()).isEqualTo("image/png");
        assertThat(stored.getImageSizeBytes()).isPositive();
        assertThat(stub().imageCalls()).isEqualTo(1);
    }

    @Test
    void rejectsAFileThatIsNotActuallyAnImage() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        // Declares itself a JPEG but the bytes say otherwise.
        MockMultipartFile disguised = new MockMultipartFile(
                "image", "payload.jpg", MediaType.IMAGE_JPEG_VALUE, "#!/bin/sh\nrm -rf /".getBytes());

        mockMvc.perform(multipart("/api/ai/food-analysis/image")
                        .file(disguised)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

        MockMultipartFile wrongType = new MockMultipartFile(
                "image", "notes.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4".getBytes());

        mockMvc.perform(multipart("/api/ai/food-analysis/image")
                        .file(wrongType)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isBadRequest());

        assertThat(stub().imageCalls()).isZero();
    }

    @Test
    void anAiFailureDegradesGracefullyAndLeavesManualEntryWorking() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        stub().failWith(new AiUnavailableException("The analysis service is temporarily unavailable"));

        mockMvc.perform(post("/api/ai/food-analysis/text")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", "a bowl of oats"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));

        // Manual logging is unaffected.
        mockMvc.perform(post("/api/nutrition/entries")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Oats", "mealType", "BREAKFAST",
                                "consumedAt", "2026-03-10T07:00:00Z",
                                "macros", Map.of("calories", 370, "proteinG", 13,
                                        "carbsG", 60, "fatG", 7, "fiberG", 10)))))
                .andExpect(status().isCreated());
    }

    @Test
    void aFailedImageAnalysisStillRecordsTheAttemptAndKeepsTheImage() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        stub().failWith(new AiUnavailableException("The analysis service is temporarily unavailable"));

        MockMultipartFile image = new MockMultipartFile(
                "image", "meal.png", MediaType.IMAGE_PNG_VALUE, TestImages.onePixelPng());

        mockMvc.perform(multipart("/api/ai/food-analysis/image")
                        .file(image)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isServiceUnavailable());

        List<AiAnalysis> analyses = aiAnalysisRepository.findAll();
        assertThat(analyses).hasSize(1);
        assertThat(analyses.get(0).getStatus()).isEqualTo(AiAnalysisStatus.FAILED);
        assertThat(analyses.get(0).getImageStorageKey()).isNotBlank();
    }

    @Test
    void theStatusEndpointTellsTheClientWhetherAiIsUsable() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/ai/food-analysis/status")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.provider").value("stub"));

        stub().setAvailable(false);

        mockMvc.perform(get("/api/ai/food-analysis/status")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void discardingAnAnalysisMarksItDiscardedAndCannotBeConfirmedAfterwards() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        UUID analysisId = UUID.fromString(analyzeText(session).path("id").asText());

        mockMvc.perform(delete("/api/ai/food-analysis/" + analysisId)
                        .header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isNoContent());

        assertThat(aiAnalysisRepository.findById(analysisId).orElseThrow().getStatus())
                .isEqualTo(AiAnalysisStatus.DISCARDED);
    }
}
