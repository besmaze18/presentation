-- WHOOP integration: the OAuth grant, single-use state values, and the imported data.
--
-- Every imported table carries the WHOOP identifier under a unique index scoped to the user, which
-- is what makes repeated synchronisation idempotent. The untouched payload is kept in JSONB so a
-- field WHOOP adds later is not lost before the schema catches up.

CREATE TABLE whoop_connections (
    id                        UUID PRIMARY KEY,
    user_id                   UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    whoop_user_id             BIGINT        NOT NULL,
    whoop_email               VARCHAR(320),
    whoop_first_name          VARCHAR(120),
    whoop_last_name           VARCHAR(120),
    -- Encrypted at rest with AES-GCM; the plaintext never touches the database or a log line.
    access_token_encrypted    VARCHAR(2048) NOT NULL,
    refresh_token_encrypted   VARCHAR(2048),
    access_token_expires_at   TIMESTAMPTZ,
    scopes                    VARCHAR(500),
    status                    VARCHAR(32)   NOT NULL DEFAULT 'CONNECTED',
    connected_at              TIMESTAMPTZ   NOT NULL,
    last_sync_at              TIMESTAMPTZ,
    last_synced_through       TIMESTAMPTZ,
    last_sync_error           VARCHAR(500),
    created_at                TIMESTAMPTZ   NOT NULL,
    updated_at                TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ux_whoop_connections_user UNIQUE (user_id),
    -- One WHOOP account maps to exactly one application account.
    CONSTRAINT ux_whoop_connections_whoop_user UNIQUE (whoop_user_id)
);

CREATE INDEX ix_whoop_connections_status ON whoop_connections (status);

CREATE TABLE whoop_oauth_states (
    id          UUID PRIMARY KEY,
    state       VARCHAR(128) NOT NULL,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ux_whoop_oauth_states_state UNIQUE (state)
);

CREATE INDEX ix_whoop_oauth_states_expiry ON whoop_oauth_states (expires_at);

CREATE TABLE whoop_cycles (
    id                  UUID PRIMARY KEY,
    user_id             UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    whoop_cycle_id      BIGINT         NOT NULL,
    start_at            TIMESTAMPTZ    NOT NULL,
    end_at              TIMESTAMPTZ,
    cycle_date          DATE           NOT NULL,
    timezone_offset     VARCHAR(16),
    score_state         VARCHAR(32),
    strain              NUMERIC(5, 2),
    kilojoules          NUMERIC(10, 2),
    average_heart_rate  INTEGER,
    max_heart_rate      INTEGER,
    raw_payload         JSONB,
    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL,
    CONSTRAINT ux_whoop_cycles_external UNIQUE (user_id, whoop_cycle_id)
);

CREATE INDEX ix_whoop_cycles_user_date ON whoop_cycles (user_id, cycle_date);

CREATE TABLE whoop_recoveries (
    id                  UUID PRIMARY KEY,
    user_id             UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    whoop_cycle_id      BIGINT         NOT NULL,
    whoop_sleep_id      VARCHAR(64),
    recovery_date       DATE           NOT NULL,
    recorded_at         TIMESTAMPTZ,
    score_state         VARCHAR(32),
    user_calibrating    BOOLEAN,
    recovery_score      INTEGER,
    resting_heart_rate  NUMERIC(6, 2),
    hrv_rmssd_milli     NUMERIC(8, 3),
    spo2_percentage     NUMERIC(5, 2),
    skin_temp_celsius   NUMERIC(5, 2),
    raw_payload         JSONB,
    created_at          TIMESTAMPTZ    NOT NULL,
    updated_at          TIMESTAMPTZ    NOT NULL,
    CONSTRAINT ux_whoop_recoveries_external UNIQUE (user_id, whoop_cycle_id),
    CONSTRAINT ck_whoop_recoveries_score CHECK (recovery_score IS NULL OR recovery_score BETWEEN 0 AND 100)
);

CREATE INDEX ix_whoop_recoveries_user_date ON whoop_recoveries (user_id, recovery_date);

CREATE TABLE whoop_sleeps (
    id                                UUID PRIMARY KEY,
    user_id                           UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    whoop_sleep_id                    VARCHAR(64)   NOT NULL,
    whoop_cycle_id                    BIGINT,
    start_at                          TIMESTAMPTZ   NOT NULL,
    end_at                            TIMESTAMPTZ,
    -- The day the sleep is attributed to: the calendar day it ended on.
    sleep_date                        DATE          NOT NULL,
    timezone_offset                   VARCHAR(16),
    nap                               BOOLEAN       NOT NULL DEFAULT FALSE,
    score_state                       VARCHAR(32),
    total_in_bed_time_millis          BIGINT,
    total_awake_time_millis           BIGINT,
    total_light_sleep_time_millis     BIGINT,
    total_slow_wave_sleep_time_millis BIGINT,
    total_rem_sleep_time_millis       BIGINT,
    sleep_duration_millis             BIGINT,
    sleep_need_millis                 BIGINT,
    disturbance_count                 INTEGER,
    sleep_cycle_count                 INTEGER,
    respiratory_rate                  NUMERIC(6, 3),
    sleep_performance_percentage      INTEGER,
    sleep_consistency_percentage      INTEGER,
    sleep_efficiency_percentage       NUMERIC(6, 3),
    raw_payload                       JSONB,
    created_at                        TIMESTAMPTZ   NOT NULL,
    updated_at                        TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ux_whoop_sleeps_external UNIQUE (user_id, whoop_sleep_id)
);

CREATE INDEX ix_whoop_sleeps_user_date ON whoop_sleeps (user_id, sleep_date, nap);

CREATE TABLE whoop_workouts (
    id                    UUID PRIMARY KEY,
    user_id               UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    whoop_workout_id      VARCHAR(64)    NOT NULL,
    start_at              TIMESTAMPTZ    NOT NULL,
    end_at                TIMESTAMPTZ,
    workout_date          DATE           NOT NULL,
    timezone_offset       VARCHAR(16),
    sport_id              INTEGER,
    sport_name            VARCHAR(80),
    score_state           VARCHAR(32),
    strain                NUMERIC(5, 2),
    kilojoules            NUMERIC(10, 2),
    average_heart_rate    INTEGER,
    max_heart_rate        INTEGER,
    distance_meters       NUMERIC(12, 3),
    altitude_gain_meters  NUMERIC(10, 3),
    percent_recorded      NUMERIC(6, 2),
    -- The training session this workout was projected into, so a re-sync updates it in place.
    training_session_id   UUID REFERENCES training_sessions (id) ON DELETE SET NULL,
    raw_payload           JSONB,
    created_at            TIMESTAMPTZ    NOT NULL,
    updated_at            TIMESTAMPTZ    NOT NULL,
    CONSTRAINT ux_whoop_workouts_external UNIQUE (user_id, whoop_workout_id)
);

CREATE INDEX ix_whoop_workouts_user_date ON whoop_workouts (user_id, workout_date);
