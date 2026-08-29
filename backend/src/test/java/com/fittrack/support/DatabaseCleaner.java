package com.fittrack.support;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Empties every table between committed tests, which is what replaces the rollback that
 * {@link IntegrationTest} would normally provide.
 */
@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;
    private List<String> tables = List.of();

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void discoverTables() {
        tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables "
                        + "where table_schema in ('PUBLIC', 'public') and table_type = 'BASE TABLE'",
                String.class);
    }

    /** Truncates everything, ignoring foreign keys for the duration. */
    public void clean() {
        if (tables.isEmpty()) {
            return;
        }
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        try {
            for (String table : tables) {
                if (table.equalsIgnoreCase("flyway_schema_history")) {
                    continue;
                }
                jdbcTemplate.execute("TRUNCATE TABLE \"" + table + "\"");
            }
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }
}
