package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.LockerSizeType;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSizeTypeConverterTest {

    private final LockerSizeTypeConverter converter = new LockerSizeTypeConverter();

    @Test
    @DisplayName("복수 LockerSize를 DB 문자열로 저장한다")
    void convertToDatabaseColumnWithMultipleValues() {
        String result = converter.convertToDatabaseColumn(Set.of(LockerSizeType.SMALL, LockerSizeType.BIG));

        assertThat(result).isEqualTo("BIG,SMALL");
    }

    @Test
    @DisplayName("DB의 복수 LockerSize 문자열을 모두 복원한다")
    void convertToEntityAttributeWithMultipleValues() {
        Set<LockerSizeType> result = converter.convertToEntityAttribute("SMALL,BIG");

        assertThat(result).containsExactly(LockerSizeType.SMALL, LockerSizeType.BIG);
    }

    @Test
    @DisplayName("LARGE 별칭과 중복값을 허용하고 정규화한다")
    void convertToEntityAttributeWithAliasAndDuplicate() {
        Set<LockerSizeType> result = converter.convertToEntityAttribute("LARGE,SMALL,BIG,SMALL");

        assertThat(result).containsExactly(LockerSizeType.SMALL, LockerSizeType.BIG);
    }

    @Test
    @DisplayName("비어있는 값은 빈 Set으로 변환한다")
    void convertBlankDataToEmptySet() {
        Set<LockerSizeType> result = converter.convertToEntityAttribute(" ");

        assertThat(result).isEmpty();
    }
}
