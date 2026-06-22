package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminLockerReportTranslationTemplateTest {

    @Test
    void showsReportThenDocumentStyleEditableTranslationReview() throws IOException {
        String translations = template("templates/admin/locker-report-translations.html");
        String list = template("templates/admin/locker-report-list.html");
        String review = template("templates/admin/locker-report-review.html");
        String layout = template("templates/admin/layout.html");

        assertThat(translations).contains(
            "page.appliedPlaceName",
            "page.appliedLockerName",
            "draft.placeTranslationFor",
            "draft.lockerTranslationFor",
            "translationsForm",
            "placeTranslations[",
            "lockerTranslations[",
            "apply-language-draft",
            "regenerate-language-draft",
            "data-redraft-url",
            "draft-content",
            "draft-error",
            "applyAllDrafts",
            "saveAllTranslations",
            "beforeunload",
            "meta[name=\"_csrf\"]",
            "response.data",
            "updateDraftRow",
            "번역 초안 / 저장된 번역",
            "th:if=\"${draft != null and page.report.status.name() != 'APPROVED'}\"",
            "th:if=\"${page.report.status.name() != 'APPROVED'}\"",
            "th:readonly=\"${page.report.status.name() == 'APPROVED'}\""
        );
        assertThat(translations).doesNotContain(
            "grid-template-columns: minmax(280px, 0.8fr)",
            "/translations/complete",
            "기존 i18n API로 저장",
            "page.report.groundLevelType",
            "page.report.priceType",
            "page.report.operatingTimeType"
        );
        assertThat(translations).contains(
            "page.report.floorLabel()",
            "page.report.priceLabel()",
            "page.report.operatingTimeLabel()"
        );
        assertThat(list).contains(
            "적용일시",
            "report.appliedAt",
            "status-badge",
            "report.status.displayName()",
            "보관함 이름 미설정"
        );
        assertThat(list).doesNotContain(
            ">관리<",
            "운영 반영 대상",
            "report.appliedPlaceId",
            "report.appliedLockerId",
            ">작업<",
            "UNDER_REVIEW",
            "이름 미입력"
        );
        assertThat(review).contains("status-badge", "page.report.status.displayName()");
        assertThat(review).doesNotContain("UNDER_REVIEW");
        assertThat(translations).contains("status-badge", "page.report.status.displayName()");
        assertThat(layout).contains(
            "status-submitted",
            "status-translation-required",
            "status-ready-for-approval",
            "status-approved",
            "status-rejected"
        );
    }

    @Test
    void formatsAppliedAtAndFallsBackWhenMissing() throws IOException {
        String list = template("templates/admin/locker-report-list.html");

        assertThat(list).contains(
            "${report.appliedAt != null} ? "
                + "${#temporals.format(report.appliedAt, 'yyyy-MM-dd HH:mm')} : '-'"
        );
    }

    private String template(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }
}
