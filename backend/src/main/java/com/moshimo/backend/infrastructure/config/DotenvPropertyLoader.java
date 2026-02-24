package com.moshimo.backend.infrastructure.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Loads a .env file into system properties before Spring Boot starts.
 *
 * Called from BackendApplication.main() — runs before any Spring machinery,
 * so every @Value("${KEY}") and YAML placeholder ${KEY:default} sees the
 * values immediately.
 *
 * spring-dotenv (4.0.0) used spring.factories which Spring Boot 4.x no longer
 * processes. EnvironmentPostProcessor is deprecated in 4.x and its .imports
 * file is not reliably loaded. Loading in main() works across all versions.
 *
 * Lookup order:
 *   1. {working-dir}/.env  (when running from backend/)
 *   2. {working-dir}/../.env  (project root fallback)
 *
 * Existing system/env properties always win.
 */
public final class DotenvPropertyLoader {

    private DotenvPropertyLoader() {}

    public static void load() {
        Path dotEnvPath = findDotEnv();
        if (dotEnvPath == null) {
            return;
        }

        try {
            Files.lines(dotEnvPath)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> line.contains("="))
                    .forEach(line -> {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    });
        } catch (IOException e) {
            System.err.println("[DotenvPropertyLoader] failed to read " + dotEnvPath + ": " + e.getMessage());
        }
    }

    private static Path findDotEnv() {
        Path cwd = Paths.get(System.getProperty("user.dir")).resolve(".env");
        if (Files.exists(cwd)) {
            return cwd;
        }

        Path parent = Paths.get(System.getProperty("user.dir")).getParent();
        if (parent != null) {
            Path rootEnv = parent.resolve(".env");
            if (Files.exists(rootEnv)) {
                return rootEnv;
            }
        }

        return null;
    }
}