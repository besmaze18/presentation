-- Core identity: users, their settings, dated nutrition goals, refresh tokens and body weight.

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    display_name    VARCHAR(120) NOT NULL,
    role            VARCHAR(32)  NOT NULL DEFAULT 'USER',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);

-- Case-insensitive uniqueness: logins are matched with LOWER(email).
CREATE UNIQUE INDEX ux_users_email_lower ON users (LOWER(email));

CREATE TABLE user_settings (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    time_zone       VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    unit_system     VARCHAR(16)  NOT NULL DEFAULT 'METRIC',
    sex             VARCHAR(16)  NOT NULL DEFAULT 'UNSPECIFIED',
    birth_date      DATE,
    height_cm       NUMERIC(5, 1),
    activity_level  VARCHAR(24)  NOT NULL DEFAULT 'MODERATE',
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ux_user_settings_user UNIQUE (user_id)
);

CREATE TABLE nutrition_goals (
    id                      UUID PRIMARY KEY,
    user_id                 UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    effective_from          DATE          NOT NULL,
    calorie_target          INTEGER       NOT NULL,
    protein_target_g        NUMERIC(6, 1) NOT NULL,
    carbs_target_g          NUMERIC(6, 1) NOT NULL,
    fat_target_g            NUMERIC(6, 1) NOT NULL,
    fiber_target_g          NUMERIC(6, 1) NOT NULL,
    target_body_weight_kg   NUMERIC(5, 2),
    created_at              TIMESTAMPTZ   NOT NULL,
    updated_at              TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ux_nutrition_goals_user_date UNIQUE (user_id, effective_from),
    CONSTRAINT ck_nutrition_goals_calories CHECK (calorie_target BETWEEN 800 AND 12000)
);

-- Resolving "the goal in effect on day X" is a descending scan on this index.
CREATE INDEX ix_nutrition_goals_user_effective ON nutrition_goals (user_id, effective_from DESC);

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash      VARCHAR(64)  NOT NULL,
    family_id       UUID         NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL,
    revoked_at      TIMESTAMPTZ,
    replaced_by_id  UUID,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ux_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX ix_refresh_tokens_expiry ON refresh_tokens (expires_at);

CREATE TABLE body_measurements (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    recorded_at         TIMESTAMPTZ   NOT NULL,
    weight_kg           NUMERIC(6, 2) NOT NULL,
    body_fat_percentage NUMERIC(5, 2),
    source              VARCHAR(32)   NOT NULL DEFAULT 'MANUAL',
    external_id         VARCHAR(128),
    note                VARCHAR(500),
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_body_measurements_weight CHECK (weight_kg > 0 AND weight_kg < 700)
);

CREATE INDEX ix_body_measurements_user_time ON body_measurements (user_id, recorded_at DESC);

-- Imported measurements must not be duplicated by repeated synchronisation runs.
CREATE UNIQUE INDEX ux_body_measurements_external
    ON body_measurements (user_id, source, external_id)
    WHERE external_id IS NOT NULL;
