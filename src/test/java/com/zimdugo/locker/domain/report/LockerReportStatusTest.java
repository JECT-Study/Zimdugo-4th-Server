package com.zimdugo.locker.domain.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LockerReportStatusTest {

    @Test
    void exposesOnlyReachableStatusesWithKoreanDisplayNames() {
        assertThat(LockerReportStatus.values())
            .extracting(Enum::name)
            .containsExactly(
                "SUBMITTED",
                "TRANSLATION_REQUIRED",
                "READY_FOR_APPROVAL",
                "APPROVED",
                "REJECTED"
            );

        assertThat(Arrays.stream(LockerReportStatus.values()).map(this::displayName))
            .containsExactly("제보 접수", "번역 필요", "승인 대기", "승인 완료", "반려");
    }

    private String displayName(LockerReportStatus status) {
        try {
            return (String) LockerReportStatus.class.getMethod("displayName").invoke(status);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return null;
        }
    }
}
