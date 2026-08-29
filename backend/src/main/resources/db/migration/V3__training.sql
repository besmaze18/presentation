-- Training: sessions plus the exercise/set structure that strength tracking will build on.

CREATE TABLE training_sessions (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title               VARCHAR(200)  NOT NULL,
    category            VARCHAR(24)   NOT NULL DEFAULT 'OTHER',
    sport_label         VARCHAR(80),
    started_at          TIMESTAMPTZ   NOT NULL,
    ended_at            TIMESTAMPTZ,
    session_date        DATE          NOT NULL,
    duration_minutes    INTEGER       NOT NULL DEFAULT 0,
    perceived_exertion  INTEGER,
    calories_kcal       NUMERIC(8, 2),
    strain              NUMERIC(5, 2),
    average_heart_rate  INTEGER,
    max_heart_rate      INTEGER,
    distance_meters     NUMERIC(10, 2),
    notes               VARCHAR(2000),
    source              VARCHAR(24)   NOT NULL DEFAULT 'MANUAL',
    external_id         VARCHAR(128),
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_training_sessions_rpe CHECK (perceived_exertion IS NULL OR perceived_exertion BETWEEN 1 AND 10),
    CONSTRAINT ck_training_sessions_duration CHECK (duration_minutes >= 0 AND duration_minutes <= 1440)
);

CREATE INDEX ix_training_sessions_user_date ON training_sessions (user_id, session_date, started_at);
CREATE INDEX ix_training_sessions_user_started ON training_sessions (user_id, started_at DESC);

-- Re-running a wearable sync must update the existing row rather than insert a duplicate.
CREATE UNIQUE INDEX ux_training_sessions_external
    ON training_sessions (user_id, source, external_id)
    WHERE external_id IS NOT NULL;

CREATE TABLE exercises (
    id                  UUID PRIMARY KEY,
    training_session_id UUID          NOT NULL REFERENCES training_sessions (id) ON DELETE CASCADE,
    position            INTEGER       NOT NULL DEFAULT 0,
    name                VARCHAR(160)  NOT NULL,
    notes               VARCHAR(500),
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_exercises_session ON exercises (training_session_id, position);

CREATE TABLE exercise_sets (
    id                  UUID PRIMARY KEY,
    exercise_id         UUID          NOT NULL REFERENCES exercises (id) ON DELETE CASCADE,
    position            INTEGER       NOT NULL DEFAULT 0,
    repetitions         INTEGER,
    weight_kg           NUMERIC(7, 2),
    rpe                 NUMERIC(3, 1),
    distance_meters     NUMERIC(10, 2),
    duration_seconds    INTEGER,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_exercise_sets_reps CHECK (repetitions IS NULL OR repetitions >= 0)
);

CREATE INDEX ix_exercise_sets_exercise ON exercise_sets (exercise_id, position);
