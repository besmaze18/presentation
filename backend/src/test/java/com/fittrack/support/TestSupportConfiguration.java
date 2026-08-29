package com.fittrack.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(ApiTestClient.class)
public class TestSupportConfiguration {}
