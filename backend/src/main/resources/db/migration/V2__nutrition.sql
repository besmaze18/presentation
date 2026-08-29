-- Nutrition: logged meals, their component items, and reusable saved foods.

CREATE TABLE food_entries (
    id              UUID PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name            VARCHAR(200)  NOT NULL,
    meal_type       VARCHAR(24)   NOT NULL DEFAULT 'OTHER',
    consumed_at     TIMESTAMPTZ   NOT NULL,
    -- The calendar date in the user's own time zone at the moment of logging. Denormalised so a
    -- day's totals are one indexed aggregate, and so history stays stable across time-zone changes.
    entry_date      DATE          NOT NULL,
    quantity        NUMERIC(8, 2),
    unit            VARCHAR(32),
    calories        NUMERIC(8, 2) NOT NULL DEFAULT 0,
    protein_g       NUMERIC(7, 2) NOT NULL DEFAULT 0,
    carbs_g         NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fat_g           NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fiber_g         NUMERIC(7, 2) NOT NULL DEFAULT 0,
    source          VARCHAR(24)   NOT NULL DEFAULT 'MANUAL',
    ai_analysis_id  UUID,
    saved_food_id   UUID,
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_food_entries_macros CHECK (
        calories >= 0 AND protein_g >= 0 AND carbs_g >= 0 AND fat_g >= 0 AND fiber_g >= 0
    )
);

-- Serves both "entries for a day" and the day-range aggregation behind the analytics endpoints.
CREATE INDEX ix_food_entries_user_date ON food_entries (user_id, entry_date, consumed_at);
CREATE INDEX ix_food_entries_user_consumed ON food_entries (user_id, consumed_at DESC);
CREATE INDEX ix_food_entries_ai_analysis ON food_entries (ai_analysis_id) WHERE ai_analysis_id IS NOT NULL;

CREATE TABLE food_items (
    id              UUID PRIMARY KEY,
    food_entry_id   UUID          NOT NULL REFERENCES food_entries (id) ON DELETE CASCADE,
    position        INTEGER       NOT NULL DEFAULT 0,
    name            VARCHAR(200)  NOT NULL,
    quantity        NUMERIC(8, 2),
    unit            VARCHAR(32),
    calories        NUMERIC(8, 2) NOT NULL DEFAULT 0,
    protein_g       NUMERIC(7, 2) NOT NULL DEFAULT 0,
    carbs_g         NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fat_g           NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fiber_g         NUMERIC(7, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL
);

CREATE INDEX ix_food_items_entry ON food_items (food_entry_id, position);

CREATE TABLE saved_foods (
    id                  UUID PRIMARY KEY,
    user_id             UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name                VARCHAR(200)  NOT NULL,
    brand               VARCHAR(120),
    serving_quantity    NUMERIC(8, 2) NOT NULL DEFAULT 1,
    serving_unit        VARCHAR(32)   NOT NULL DEFAULT 'serving',
    calories            NUMERIC(8, 2) NOT NULL DEFAULT 0,
    protein_g           NUMERIC(7, 2) NOT NULL DEFAULT 0,
    carbs_g             NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fat_g               NUMERIC(7, 2) NOT NULL DEFAULT 0,
    fiber_g             NUMERIC(7, 2) NOT NULL DEFAULT 0,
    default_meal_type   VARCHAR(24),
    usage_count         BIGINT        NOT NULL DEFAULT 0,
    last_used_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_saved_foods_serving CHECK (serving_quantity > 0)
);

-- One saved food per name per user, matched case-insensitively.
CREATE UNIQUE INDEX ux_saved_foods_user_name ON saved_foods (user_id, LOWER(name));
CREATE INDEX ix_saved_foods_user_usage ON saved_foods (user_id, usage_count DESC, last_used_at DESC);

ALTER TABLE food_entries
    ADD CONSTRAINT fk_food_entries_saved_food
    FOREIGN KEY (saved_food_id) REFERENCES saved_foods (id) ON DELETE SET NULL;
