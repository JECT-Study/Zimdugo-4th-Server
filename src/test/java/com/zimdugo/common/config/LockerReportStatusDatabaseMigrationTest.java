package com.zimdugo.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LockerReportStatusDatabaseMigrationTest {

    private static final Path MIGRATION = Path.of("scripts/db/02-locker-report-status.sql");

    @Test
    void localProfileAppliesLockerReportStatusMigration() throws IOException {
        assertThat(MIGRATION).exists();

        String localConfiguration = Files.readString(
            Path.of("src/main/resources/application-local.yaml"),
            StandardCharsets.UTF_8
        );

        assertThat(localConfiguration)
            .contains("file:scripts/db/02-locker-report-status.sql");
    }

    @Test
    void migrationAllowsEveryLockerReportStatus() throws IOException {
        assertThat(MIGRATION).exists();
        String migration = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(LockerReportStatus.values())
            .allSatisfy(status -> assertThat(migration).contains("'" + status.name() + "'"));
    }
}
