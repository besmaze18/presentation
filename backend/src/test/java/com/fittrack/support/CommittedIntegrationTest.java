package com.fittrack.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * An integration test whose writes really commit.
 *
 * <p>{@link IntegrationTest} wraps each test in a transaction that is rolled back afterwards, which
 * is fast and keeps tests isolated — but it silently changes behaviour for any code that commits
 * separately from the request, and for any code whose state must survive a thrown exception.
 * Testing those paths under a wrapping transaction produces false passes: the rollback that would
 * discard the write in production never happens, and a {@code REQUIRES_NEW} transaction cannot see
 * the uncommitted rows it is supposed to act on.
 *
 * <p>So these tests commit for real and {@link DatabaseCleaner} truncates between them.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(DatabaseCleaner.class)
public @interface CommittedIntegrationTest {}
