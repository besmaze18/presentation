package com.fittrack.whoop;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/** A stand-in for the WHOOP API so synchronisation is exercised without a network dependency. */
final class WhoopApiStub {

    static final long WHOOP_USER_ID = 10129L;
    static final long CYCLE_ID = 93845L;
    static final String SLEEP_ID = "5c060dd1-975d-4544-880c-3def81bdfb0d";
    static final String WORKOUT_ID = "ecfc6a15-4661-442f-a9a4-f160dd7afae8";

    private final WireMockServer server;

    private WhoopApiStub(WireMockServer server) {
        this.server = server;
    }

    static WhoopApiStub start() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        return new WhoopApiStub(server);
    }

    int port() {
        return server.port();
    }

    void stop() {
        server.stop();
    }

    void reset() {
        server.resetAll();
    }

    WireMockServer server() {
        return server;
    }

    void stubTokenExchange(String accessToken, String refreshToken, long expiresIn) {
        server.stubFor(post(urlPathEqualTo("/oauth/oauth2/token"))
                .willReturn(json(
                        """
                        {"access_token":"%s","refresh_token":"%s","expires_in":%d,
                         "token_type":"bearer","scope":"read:profile read:cycles offline"}
                        """
                                .formatted(accessToken, refreshToken, expiresIn))));
    }

    void stubTokenExchangeFailure(int status) {
        server.stubFor(post(urlPathEqualTo("/oauth/oauth2/token"))
                .willReturn(aResponse().withStatus(status).withBody("{\"error\":\"invalid_grant\"}")));
    }

    void stubProfile() {
        server.stubFor(get(urlPathEqualTo("/developer/v2/user/profile/basic"))
                .willReturn(json(
                        """
                        {"user_id":%d,"email":"athlete@whoop.test","first_name":"Alex","last_name":"Rivera"}
                        """
                                .formatted(WHOOP_USER_ID))));
    }

    void stubBodyMeasurement(double weightKg) {
        server.stubFor(get(urlPathEqualTo("/developer/v2/user/measurement/body"))
                .willReturn(json(
                        """
                        {"height_meter":1.8288,"weight_kilogram":%s,"max_heart_rate":195}
                        """
                                .formatted(weightKg))));
    }

    /** Stubs all four collections with a single record each, all on 2026-03-10. */
    void stubCollections() {
        stubCollections(14.2, 62, 7 * 3_600_000L + 20 * 60_000L);
    }

    void stubCollections(double dayStrain, int recoveryScore, long inBedMillis) {
        server.stubFor(get(urlPathEqualTo("/developer/v2/cycle"))
                .willReturn(json(
                        """
                        {"records":[{"id":%d,"user_id":%d,"created_at":"2026-03-10T11:25:44.774Z",
                          "updated_at":"2026-03-10T14:25:44.774Z","start":"2026-03-10T04:00:00.000Z",
                          "end":"2026-03-11T03:59:00.000Z","timezone_offset":"+01:00",
                          "score_state":"SCORED",
                          "score":{"strain":%s,"kilojoule":11757.0,"average_heart_rate":68,
                                   "max_heart_rate":171}}],
                         "next_token":null}
                        """
                                .formatted(CYCLE_ID, WHOOP_USER_ID, dayStrain))));

        server.stubFor(get(urlPathEqualTo("/developer/v2/recovery"))
                .willReturn(json(
                        """
                        {"records":[{"cycle_id":%d,"sleep_id":"%s","user_id":%d,
                          "created_at":"2026-03-10T11:25:44.774Z","updated_at":"2026-03-10T14:25:44.774Z",
                          "score_state":"SCORED",
                          "score":{"user_calibrating":false,"recovery_score":%d,"resting_heart_rate":48,
                                   "hrv_rmssd_milli":91.813562,"spo2_percentage":96.5,
                                   "skin_temp_celsius":33.7}}],
                         "next_token":null}
                        """
                                .formatted(CYCLE_ID, SLEEP_ID, WHOOP_USER_ID, recoveryScore))));

        server.stubFor(get(urlPathEqualTo("/developer/v2/activity/sleep"))
                .willReturn(json(
                        """
                        {"records":[{"id":"%s","user_id":%d,"created_at":"2026-03-10T11:25:44.774Z",
                          "updated_at":"2026-03-10T14:25:44.774Z","start":"2026-03-09T22:30:00.000Z",
                          "end":"2026-03-10T06:10:00.000Z","timezone_offset":"+01:00","nap":false,
                          "score_state":"SCORED",
                          "score":{"stage_summary":{"total_in_bed_time_milli":%d,
                                     "total_awake_time_milli":1200000,
                                     "total_light_sleep_time_milli":14400000,
                                     "total_slow_wave_sleep_time_milli":5400000,
                                     "total_rem_sleep_time_milli":5400000,
                                     "sleep_cycle_count":5,"disturbance_count":3},
                                   "sleep_needed":{"baseline_milli":27000000,
                                     "need_from_sleep_debt_milli":1800000,
                                     "need_from_recent_strain_milli":900000,
                                     "need_from_recent_nap_milli":0},
                                   "respiratory_rate":14.11,"sleep_performance_percentage":92,
                                   "sleep_consistency_percentage":81,
                                   "sleep_efficiency_percentage":95.2}}],
                         "next_token":null}
                        """
                                .formatted(SLEEP_ID, WHOOP_USER_ID, inBedMillis))));

        server.stubFor(get(urlPathEqualTo("/developer/v2/activity/workout"))
                .willReturn(json(
                        """
                        {"records":[{"id":"%s","user_id":%d,"created_at":"2026-03-10T18:25:44.774Z",
                          "updated_at":"2026-03-10T19:25:44.774Z","start":"2026-03-10T17:00:00.000Z",
                          "end":"2026-03-10T18:05:00.000Z","timezone_offset":"+01:00",
                          "sport_id":45,"sport_name":"Weightlifting","score_state":"SCORED",
                          "score":{"strain":12.4,"average_heart_rate":123,"max_heart_rate":168,
                                   "kilojoule":2175.0,"percent_recorded":100,
                                   "distance_meter":0,"altitude_gain_meter":0,
                                   "zone_duration":{"zone_zero_milli":0}}}],
                         "next_token":null}
                        """
                                .formatted(WORKOUT_ID, WHOOP_USER_ID))));
    }

    /** Two pages of cycles, to exercise next_token pagination. */
    void stubPaginatedCycles() {
        server.stubFor(get(urlPathEqualTo("/developer/v2/cycle"))
                .withQueryParam("nextToken", com.github.tomakehurst.wiremock.client.WireMock.absent())
                .willReturn(json(
                        """
                        {"records":[{"id":1,"start":"2026-03-08T04:00:00.000Z",
                          "end":"2026-03-09T03:59:00.000Z","score_state":"SCORED",
                          "score":{"strain":8.1,"kilojoule":9000.0}}],
                         "next_token":"page-2"}
                        """)));

        server.stubFor(get(urlPathEqualTo("/developer/v2/cycle"))
                .withQueryParam("nextToken", com.github.tomakehurst.wiremock.client.WireMock.equalTo("page-2"))
                .willReturn(json(
                        """
                        {"records":[{"id":2,"start":"2026-03-09T04:00:00.000Z",
                          "end":"2026-03-10T03:59:00.000Z","score_state":"SCORED",
                          "score":{"strain":11.3,"kilojoule":10000.0}}],
                         "next_token":null}
                        """)));

        stubEmptyCollection("/developer/v2/recovery");
        stubEmptyCollection("/developer/v2/activity/sleep");
        stubEmptyCollection("/developer/v2/activity/workout");
    }

    void stubEmptyCollection(String path) {
        server.stubFor(get(urlPathEqualTo(path))
                .willReturn(json("{\"records\":[],\"next_token\":null}")));
    }

    void stubEmptyCollections() {
        stubEmptyCollection("/developer/v2/cycle");
        stubEmptyCollection("/developer/v2/recovery");
        stubEmptyCollection("/developer/v2/activity/sleep");
        stubEmptyCollection("/developer/v2/activity/workout");
    }

    void stubCollectionFailure(String path, int status) {
        server.stubFor(get(urlPathEqualTo(path))
                .willReturn(aResponse().withStatus(status).withBody("{\"error\":\"nope\"}")));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(String body) {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }
}
