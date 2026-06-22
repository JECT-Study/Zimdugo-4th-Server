package com.zimdugo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LocalizedAliasDatabaseMigrationTest {

    private static final Path MIGRATION = Path.of(
        "scripts/db/03-localized-alias-language-uniqueness.sql"
    );

    @Test
    void localProfileAppliesLocalizedAliasMigration() throws IOException {
        assertThat(MIGRATION).exists();

        String localConfiguration = Files.readString(
            Path.of("src/main/resources/application-local.yaml"),
            StandardCharsets.UTF_8
        );

        assertThat(localConfiguration)
            .contains("file:scripts/db/03-localized-alias-language-uniqueness.sql");
    }

    @Test
    void migrationScopesPlaceAndLockerAliasesByLanguage() throws IOException {
        assertThat(MIGRATION).exists();
        String migration = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(migration)
            .contains("UNIQUE (place_id, language_code, normalized_alias)")
            .contains("UNIQUE (locker_id, language_code, normalized_alias)");
    }
}
