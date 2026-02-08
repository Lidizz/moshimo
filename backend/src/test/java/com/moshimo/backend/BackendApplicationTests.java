package com.moshimo.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Application context test.
 * Disabled in CI to avoid requiring full database setup.
 * Enable locally with a running PostgreSQL instance.
 */
@SpringBootTest
class BackendApplicationTests {

    @Test
    @Disabled("Requires PostgreSQL database - enable for local integration testing")
    void contextLoads() {
    }

}
