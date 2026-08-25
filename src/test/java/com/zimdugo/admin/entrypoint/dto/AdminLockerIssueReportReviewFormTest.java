package com.zimdugo.admin.entrypoint.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminLockerIssueReportReviewFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("처리 메모는 최대 1000자까지 허용한다")
    void reviewMemoMaxLength() {
        AdminLockerIssueReportReviewForm form = new AdminLockerIssueReportReviewForm();
        form.setReviewMemo("a".repeat(1001));

        Set<ConstraintViolation<AdminLockerIssueReportReviewForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("처리 메모는 비어 있어도 된다")
    void reviewMemoIsOptional() {
        AdminLockerIssueReportReviewForm form = new AdminLockerIssueReportReviewForm();
        form.setReviewMemo(null);

        Set<ConstraintViolation<AdminLockerIssueReportReviewForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }
}
