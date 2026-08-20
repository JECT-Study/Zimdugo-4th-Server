package com.zimdugo.locker.entrypoint.dto.request.issue;

import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerIssueReportCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("신고 유형은 필수다")
    void reportTypeIsRequired() {
        LockerIssueReportCreateRequest request = new LockerIssueReportCreateRequest(null, "상세 내용");

        Set<ConstraintViolation<LockerIssueReportCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("상세 내용은 없어도 된다")
    void detailIsOptional() {
        LockerIssueReportCreateRequest request = new LockerIssueReportCreateRequest(
            LockerIssueReportType.CATEGORY_ERROR,
            null
        );

        Set<ConstraintViolation<LockerIssueReportCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("상세 내용은 최대 1000자까지 허용한다")
    void detailMaxLength() {
        LockerIssueReportCreateRequest request = new LockerIssueReportCreateRequest(
            LockerIssueReportType.OTHER,
            "a".repeat(1001)
        );

        Set<ConstraintViolation<LockerIssueReportCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("명세에 정의된 신고 유형을 허용한다")
    void supportsSpecifiedReportTypes() {
        assertThat(LockerIssueReportType.values()).containsExactly(
            LockerIssueReportType.PRICE_ERROR,
            LockerIssueReportType.NO_LONGER_OPERATING,
            LockerIssueReportType.SIZE_ERROR,
            LockerIssueReportType.OPERATING_HOURS_ERROR,
            LockerIssueReportType.WRONG_LOCATION,
            LockerIssueReportType.IMAGE_ERROR,
            LockerIssueReportType.CATEGORY_ERROR,
            LockerIssueReportType.OTHER
        );
    }
}
