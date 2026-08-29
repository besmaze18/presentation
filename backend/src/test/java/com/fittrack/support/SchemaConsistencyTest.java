package com.fittrack.support;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Guards against drift between the Flyway migrations and the JPA mappings.
 *
 * <p>Production runs with {@code ddl-auto: validate}, so a table or column present in one and not
 * the other is a startup failure. The rest of the suite cannot catch that: it generates its schema
 * from the mappings, so the mappings are true by construction there. This test instead exports the
 * DDL Hibernate would need (against the PostgreSQL dialect, without a database) and compares it to
 * what the migrations actually create - which needs no Docker daemon and runs on every build.
 */
class SchemaConsistencyTest {

    private static final Path MIGRATIONS = Path.of("src/main/resources/db/migration");
    private static final String ENTITY_PACKAGE = "com.fittrack";

    private static final Pattern CREATE_TABLE =
            Pattern.compile("create table\\s+(?:if not exists\\s+)?\"?(\\w+)\"?\\s*\\((.*?)\\);",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Test
    void everyMappedTableAndColumnExistsInTheMigrations() {
        Map<String, Set<String>> mapped = exportMappedSchema();
        Map<String, Set<String>> migrated = parseMigrations();

        assertThat(mapped).isNotEmpty();
        assertThat(migrated).isNotEmpty();

        Set<String> missingTables = new TreeSet<>(mapped.keySet());
        missingTables.removeAll(migrated.keySet());
        assertThat(missingTables)
                .as("tables the entities expect but no migration creates")
                .isEmpty();

        Map<String, Set<String>> missingColumns = new HashMap<>();
        mapped.forEach((table, columns) -> {
            Set<String> missing = new TreeSet<>(columns);
            missing.removeAll(migrated.getOrDefault(table, Set.of()));
            if (!missing.isEmpty()) {
                missingColumns.put(table, missing);
            }
        });
        assertThat(missingColumns)
                .as("columns the entities expect but no migration creates")
                .isEmpty();
    }

    @Test
    void theMigrationsDoNotCreateTablesNothingIsMappedTo() {
        Map<String, Set<String>> mapped = exportMappedSchema();
        Set<String> orphaned = new TreeSet<>(parseMigrations().keySet());
        orphaned.removeAll(mapped.keySet());
        // flyway_schema_history is Flyway's own bookkeeping and is never mapped.
        orphaned.remove("flyway_schema_history");

        assertThat(orphaned).as("tables created by a migration that no entity maps to").isEmpty();
    }

    /**
     * The schema the mappings require, read straight from Hibernate's own metadata against the
     * PostgreSQL dialect. No database connection and no DDL text to re-parse.
     */
    private static Map<String, Set<String>> exportMappedSchema() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting(AvailableSettings.HBM2DDL_AUTO, "none")
                .build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            entityClasses().forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();

            Map<String, Set<String>> schema = new HashMap<>();
            for (var namespace : metadata.getDatabase().getNamespaces()) {
                for (Table table : namespace.getTables()) {
                    if (!table.isPhysicalTable()) {
                        continue;
                    }
                    Set<String> columns = new TreeSet<>();
                    for (Column column : table.getColumns()) {
                        columns.add(column.getName().toLowerCase(Locale.ROOT));
                    }
                    schema.put(table.getName().toLowerCase(Locale.ROOT), columns);
                }
            }
            return schema;
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static List<Class<?>> entityClasses() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        return scanner.findCandidateComponents(ENTITY_PACKAGE).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(SchemaConsistencyTest::loadClass)
                .toList();
    }

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Could not load mapped entity " + name, ex);
        }
    }

    private static Map<String, Set<String>> parseMigrations() {
        try (Stream<Path> files = Files.list(MIGRATIONS)) {
            StringBuilder combined = new StringBuilder();
            files.filter(path -> path.toString().endsWith(".sql"))
                    .sorted()
                    .forEach(path -> {
                        try {
                            combined.append(Files.readString(path)).append('\n');
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
            return parseCreateStatements(stripComments(combined.toString()));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String stripComments(String sql) {
        return sql.replaceAll("--[^\\n]*", "");
    }

    /** Extracts {@code table -> column names} from a script of CREATE TABLE statements. */
    private static Map<String, Set<String>> parseCreateStatements(String sql) {
        Map<String, Set<String>> schema = new HashMap<>();
        Matcher matcher = CREATE_TABLE.matcher(sql);
        while (matcher.find()) {
            String table = matcher.group(1).toLowerCase(Locale.ROOT);
            schema.put(table, parseColumnNames(matcher.group(2)));
        }
        return schema;
    }

    private static Set<String> parseColumnNames(String body) {
        Set<String> columns = new HashSet<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (char character : body.toCharArray()) {
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
            }
            if (character == ',' && depth == 0) {
                addColumn(columns, current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        addColumn(columns, current.toString());
        return columns;
    }

    private static void addColumn(Set<String> columns, String definition) {
        String trimmed = definition.trim().replace("\"", "");
        if (trimmed.isEmpty()) {
            return;
        }
        String firstWord = trimmed.split("\\s+")[0].toLowerCase(Locale.ROOT);
        // Skip table-level clauses: they are constraints, not columns.
        if (Set.of("primary", "foreign", "unique", "constraint", "check", "exclude").contains(firstWord)) {
            return;
        }
        columns.add(firstWord);
    }
}
