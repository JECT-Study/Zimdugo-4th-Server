package com.zimdugo.locker.entrypoint.converter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LockerFilterRequestConverterTest {

    private final LockerSizeTypeRequestConverter lockerSizeTypeRequestConverter =
        new LockerSizeTypeRequestConverter();
    private final IndoorOutdoorTypeRequestConverter indoorOutdoorTypeRequestConverter =
        new IndoorOutdoorTypeRequestConverter();
    private final LockerTypeRequestConverter lockerTypeRequestConverter =
        new LockerTypeRequestConverter();

    @Test
    @DisplayName("보관함 크기 컨버터는 소문자와 JSON 배열 토큰을 정규화한다")
    void convertLockerSizeType() {
        assertThat(lockerSizeTypeRequestConverter.convert("\"small\"")).isEqualTo(LockerSizeFilterType.SMALL);
        assertThat(lockerSizeTypeRequestConverter.convert("[LARGE]")).isEqualTo(LockerSizeFilterType.LARGE);
    }

    @Test
    @DisplayName("실내외 컨버터는 대소문자와 공백을 정규화한다")
    void convertIndoorOutdoorType() {
        assertThat(indoorOutdoorTypeRequestConverter.convert(" indoor ")).isEqualTo(IndoorOutdoorFilterType.INDOOR);
        assertThat(indoorOutdoorTypeRequestConverter.convert("\"OUTDOOR\""))
            .isEqualTo(IndoorOutdoorFilterType.OUTDOOR);
    }

    @Test
    @DisplayName("보관함 유형 컨버터는 유효하지 않은 입력을 예외로 처리한다")
    void invalidLockerTypeThrowsException() {
        assertThatThrownBy(() -> lockerTypeRequestConverter.convert("unknown"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("보관함 유형 컨버터는 JSON 배열 토큰을 정규화한다")
    void convertLockerType() {
        assertThat(lockerTypeRequestConverter.convert("[\"subway_station\"]"))
            .isEqualTo(LockerFacilityFilterType.SUBWAY_STATION);
    }
}
