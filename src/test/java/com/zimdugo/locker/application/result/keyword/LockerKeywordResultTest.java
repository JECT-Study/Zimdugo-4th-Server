package com.zimdugo.locker.application.result.keyword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerKeywordResultTest {

    @Test
    @DisplayName("null 아이템 목록이면 빈 결과를 반환한다")
    void returnsEmptyWhenItemsAreNull() {
        LockerKeywordResult result = LockerKeywordResult.of(null);

        assertThat(result.count()).isZero();
        assertThat(result.bounds()).isNull();
        assertThat(result.items()).isEmpty();
    }
}
