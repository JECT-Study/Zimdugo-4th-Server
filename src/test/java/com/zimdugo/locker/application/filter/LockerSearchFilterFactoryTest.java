package com.zimdugo.locker.application.filter;

import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSearchFilterFactoryTest {

    @Test
    @DisplayName("빈 필터 토큰이 null로 바인딩되어도 무시하고 빈 필터로 처리한다")
    void ignoresNullFilterTokens() {
        Set<LockerSizeFilterType> sizeTypes = new HashSet<>();
        sizeTypes.add(null);

        LockerSearchFilter filter = LockerSearchFilterFactory.create(sizeTypes, null, null);

        assertThat(filter.isEmpty()).isTrue();
        assertThat(filter.sizeTypes()).isEmpty();
    }
}
