-- AI food analysis: the prediction, the image reference and the user-confirmed values.
--
-- The prediction and the confirmed macros are stored side by side so estimation accuracy can be
-- measured later. Image bytes are never stored here - only the storage provider and key.

CREATE TABLE ai_analyses (
    id                      UUID PRIMARY KEY,
    user_id                 UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kind                    VARCHAR(16)   NOT NULL,
    status                  VARCHAR(24)   NOT NULL DEFAULT 'PENDING_REVIEW',
    provider                VARCHAR(64)   NOT NULL,
    model                   VARCHAR(128),

    input_text              VARCHAR(2000),

    image_storage_provider  VARCHAR(32),
    image_storage_key       VARCHAR(512),
    image_content_type      VARCHAR(64),
    image_size_bytes        BIGINT,

    predicted_meal_name     VARCHAR(200),
    predicted_calories      NUMERIC(8, 2) NOT NULL DEFAULT 0,
    predicted_protein_g     NUMERIC(7, 2) NOT NULL DEFAULT 0,
    predicted_carbs_g       NUMERIC(7, 2) NOT NULL DEFAULT 0,
    predicted_fat_g         NUMERIC(7, 2) NOT NULL DEFAULT 0,
    predicted_fiber_g       NUMERIC(7, 2) NOT NULL DEFAULT 0,
    confidence              NUMERIC(3, 2),

    -- Selective JSONB: the normalised prediction and the provider's untouched response. These are
    -- semi-structured by nature; everything the application queries on has its own column.
    prediction              JSONB,
    raw_response            JSONB,

    latency_millis          BIGINT,
    input_tokens            BIGINT,
    output_tokens           BIGINT,
    error_message           VARCHAR(500),

    confirmed_food_entry_id UUID REFERENCES food_entries (id) ON DELETE SET NULL,
    confirmed_at            TIMESTAMPTZ,
    confirmed_calories      NUMERIC(8, 2),
    confirmed_protein_g     NUMERIC(7, 2),
    confirmed_carbs_g       NUMERIC(7, 2),
    confirmed_fat_g         NUMERIC(7, 2),
    confirmed_fiber_g       NUMERIC(7, 2),

    created_at              TIMESTAMPTZ   NOT NULL,
    updated_at              TIMESTAMPTZ   NOT NULL,

    CONSTRAINT ck_ai_analyses_confidence CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 1))
);

CREATE INDEX ix_ai_analyses_user_created ON ai_analyses (user_id, created_at DESC);
CREATE INDEX ix_ai_analyses_user_status ON ai_analyses (user_id, status, created_at DESC);

-- One food entry per confirmed analysis.
CREATE UNIQUE INDEX ux_ai_analyses_food_entry
    ON ai_analyses (confirmed_food_entry_id)
    WHERE confirmed_food_entry_id IS NOT NULL;

ALTER TABLE food_entries
    ADD CONSTRAINT fk_food_entries_ai_analysis
    FOREIGN KEY (ai_analysis_id) REFERENCES ai_analyses (id) ON DELETE SET NULL;
