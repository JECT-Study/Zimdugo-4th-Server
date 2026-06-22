package com.zimdugo.locker.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import org.junit.jupiter.api.Test;

class LockerReportTranslationLifecycleTest {

    @Test
    void movesFromTranslationRequiredThroughReadyForApprovalToApproved() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");

        report.markTranslationsReady();

        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.READY_FOR_APPROVAL);
        assertThat(report.getAppliedAt()).isNull();

        report.completeApproval();

        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.APPROVED);
        assertThat(report.getAppliedAt()).isNotNull();
    }

    @Test
    void keepsReadyStateWhenTranslationsAreSavedAgain() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");
        report.markTranslationsReady();

        report.markTranslationsReady();

        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.READY_FOR_APPROVAL);
    }

    @Test
    void rejectsFinalApprovalBeforeTranslationsAreReady() {
        LockerReportEntity report = report();
        report.approve(10L, 20L, "admin");

        assertThatThrownBy(report::completeApproval)
            .isInstanceOf(BusinessException.class);
        assertThat(report.getStatus()).isEqualTo(LockerReportStatus.TRANSLATION_REQUIRED);
        assertThat(report.getAppliedAt()).isNull();
    }

    private LockerReportEntity report() {
        return LockerReportEntity.builder()
            .id(1L)
            .build();
    }
}
