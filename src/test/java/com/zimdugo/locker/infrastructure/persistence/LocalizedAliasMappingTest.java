package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LocalizedAliasMappingTest {

    @Test
    void scopesPlaceAliasUniquenessByLanguage() {
        assertThat(uniqueConstraintColumns(PlaceAliasEntity.class))
            .containsExactly("place_id", "language_code", "normalized_alias");
    }

    @Test
    void scopesLockerAliasUniquenessByLanguage() {
        assertThat(uniqueConstraintColumns(LockerAliasEntity.class))
            .containsExactly("locker_id", "language_code", "normalized_alias");
    }

    private String[] uniqueConstraintColumns(Class<?> entityType) {
        return Arrays.stream(entityType.getAnnotation(Table.class).uniqueConstraints())
            .findFirst()
            .map(UniqueConstraint::columnNames)
            .orElseThrow();
    }
}
