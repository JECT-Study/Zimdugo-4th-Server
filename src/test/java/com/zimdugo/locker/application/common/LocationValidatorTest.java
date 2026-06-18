package com.zimdugo.locker.application.common;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationValidatorTest {

    @Test
    @DisplayName("위도와 경도의 경계값을 허용한다")
    void allowsBoundaryValues() {
        assertThatCode(() -> LocationValidator.validate(-90.0, -180.0))
            .doesNotThrowAnyException();
        assertThatCode(() -> LocationValidator.validate(90.0, 180.0))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("위도 또는 경도가 범위를 벗어나면 예외가 발생한다")
    void throwsWhenLocationIsOutOfRange() {
        assertInvalidLocation(90.1, 0.0);
        assertInvalidLocation(-90.1, 0.0);
        assertInvalidLocation(0.0, 180.1);
        assertInvalidLocation(0.0, -180.1);
    }

    private void assertInvalidLocation(double latitude, double longitude) {
        assertThatThrownBy(() -> LocationValidator.validate(latitude, longitude))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_LOCATION_RANGE);
    }
}
