package com.zimdugo.common.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VisitorLogDatabaseMigrationTest {

    @Test
    void declaresVisitorTableUniquenessAndDateIndex() throws IOException {
        String sql = Files.readString(Path.of("scripts/db/02-visitor-logs.sql"));

        assertThat(sql)
            .contains("CREATE TABLE IF NOT EXISTS visitor_logs")
            .contains("visitor_identifier")
            .contains("accessed_date")
            .contains("CREATE UNIQUE INDEX IF NOT EXISTS uk_visitor_logs_identifier_date")
            .contains("CREATE INDEX IF NOT EXISTS idx_visitor_logs_accessed_date");
    }
}
