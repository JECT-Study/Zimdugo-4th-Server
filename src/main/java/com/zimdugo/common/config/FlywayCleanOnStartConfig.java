package com.zimdugo.common.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayCleanOnStartConfig {

    @Bean
    @ConditionalOnProperty(name = "app.flyway.clean-on-start", havingValue = "true")
    public FlywayMigrationStrategy cleanAndMigrateStrategy() {
        return this::cleanAndMigrate;
    }

    private void cleanAndMigrate(Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }
}
