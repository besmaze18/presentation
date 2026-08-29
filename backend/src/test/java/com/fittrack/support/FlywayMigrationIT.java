package com.fittrack.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Verifies the real Flyway migrations against a real PostgreSQL.
 *
 * <p>The rest of the suite runs on H2 with a Hibernate-generated schema so it needs no Docker
 * daemon. That is fast, but it would never catch a PostgreSQL-only mistake - a partial index, a
 * JSONB column, an expression-based unique index. This test closes that gap, and self-skips
 * (rather than failing) where no Docker daemon is available, so `mvn test` stays green on a
 * machine without one. Run it in CI, where Docker is present.
 */
class FlywayMigrationIT {

    private static final String IMAGE = "postgres:16-alpine";

    @Test
    void everyMigrationAppliesCleanlyAndIsIdempotentOnRerun() {
        if (!dockerAvailable()) {
            org.junit.jupiter.api.Assumptions.abort(
                    "No Docker daemon available - skipping the PostgreSQL migration check");
        }

        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(IMAGE)) {
            postgres.start();

            Flyway flyway = Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("classpath:db/migration")
                    .load();

            MigrateResult result = flyway.migrate();
            assertThat(result.migrationsExecuted).isPositive();
            assertThat(result.success).isTrue();

            // Running again must be a no-op, not an error.
            assertThat(flyway.migrate().migrationsExecuted).isZero();
            // And validation must agree that what ran matches what is on disk.
            flyway.validate();

            List<String> tables = query(
                    postgres,
                    "select table_name from information_schema.tables "
                            + "where table_schema = 'public' order by table_name");

            assertThat(tables)
                    .contains(
                            "users",
                            "user_settings",
                            "nutrition_goals",
                            "refresh_tokens",
                            "body_measurements",
                            "food_entries",
                            "food_items",
                            "saved_foods",
                            "training_sessions",
                            "exercises",
                            "exercise_sets",
                            "ai_analyses",
                            "whoop_connections",
                            "whoop_oauth_states",
                            "whoop_cycles",
                            "whoop_recoveries",
                            "whoop_sleeps",
                            "whoop_workouts");

            // The PostgreSQL-specific constructs the H2 suite cannot exercise.
            List<String> jsonbColumns = query(
                    postgres,
                    "select table_name || '.' || column_name from information_schema.columns "
                            + "where table_schema = 'public' and data_type = 'jsonb' order by 1");
            assertThat(jsonbColumns)
                    .contains(
                            "ai_analyses.prediction",
                            "ai_analyses.raw_response",
                            "whoop_cycles.raw_payload",
                            "whoop_workouts.raw_payload");

            List<String> indexes = query(
                    postgres, "select indexname from pg_indexes where schemaname = 'public'");
            assertThat(indexes)
                    .contains(
                            "ux_users_email_lower",
                            "ux_body_measurements_external",
                            "ux_saved_foods_user_name",
                            "ux_training_sessions_external",
                            "ux_whoop_cycles_external",
                            "ux_whoop_workouts_external");
        }
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static List<String> query(PostgreSQLContainer<?> postgres, String sql) {
        List<String> values = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException("Could not inspect the migrated schema", ex);
        }
        return values;
    }
}
