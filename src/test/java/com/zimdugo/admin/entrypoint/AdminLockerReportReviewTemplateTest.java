package com.zimdugo.admin.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AdminLockerReportReviewTemplateTest {

    @Test
    void providesPlacePoiApprovalRejectionAndTranslationActions() throws IOException {
        ClassPathResource template = new ClassPathResource("templates/admin/locker-report-review.html");
        String content = template.getContentAsString(StandardCharsets.UTF_8);
        int stepOneMarkerIndex = content.indexOf("등록할 장소 선택");
        int placeNameGroupIndex = content.indexOf("id=\"placeNameGroup\"");
        int firstCandidateListIndex = content.indexOf("class=\"candidate-list\"");

        assertThat(content).contains("existingPlaceId", "poiCandidate", "placeName", "lockerName", "승인", "거절");
        assertThat(content).contains("/translations");
        assertThat(content).contains(
            "status.name() == 'TRANSLATION_REQUIRED'",
            "번역 검수",
            "status.name() == 'READY_FOR_APPROVAL'",
            "번역 수정",
            "/approve-translations"
        );
        assertThat(content).contains("층 없음", "가격 정보 없음", "운영 시간 정보 없음", "30m 이내 장소");
        assertThat(content).contains("새 장소 이름을 직접 입력하거나 카카오 POI 후보로 채울 수 있습니다.");
        assertThat(content).contains("카카오 장소 후보", "선택한 장소 이름과 층을 기본값으로 채우며 직접 수정할 수 있습니다.");
        assertThat(content).contains("거절 메모", "name=\"rejectionMemo\"");
        assertThat(content).contains("status.name() == 'REJECTED' and page.report.rejectionMemo != null");
        assertThat(content).doesNotContain("검토 메모", "th:field=\"*{reviewNote}\"");
        assertThat(content).contains(
            "data-name=${place.name}",
            "placeNameGroup.hidden = !createsPlace",
            "poiCandidateGroup.hidden = !createsPlace",
            "setSuggestedLockerName"
        );
        assertThat(content).contains("approvalForm.dataset.floorLabel");
        assertThat(content).contains("<script th:if=\"${page.report.status.name() == 'SUBMITTED'}\">");
        assertThat(stepOneMarkerIndex).isNotNegative().isLessThan(placeNameGroupIndex);
        assertThat(placeNameGroupIndex).isLessThan(firstCandidateListIndex);
        assertThat(content).doesNotContain("lockerName: input.dataset.name");
        assertThat(content).doesNotContain("placeNameInput.readOnly = true", "lockerNameInput.placeholder");
        assertThat(content).doesNotContain("lockerNameInput.readOnly = true");
        assertThat(content).doesNotContain(
            "<strong>보관함 이름 입력</strong>"
                + "<div class=\"muted\">직접 입력하거나 카카오 POI 후보로 채울 수 있습니다.</div>"
        );
        assertThat(content).doesNotContain("주소 일치 또는 200m 이내 장소");
        assertThat(content).doesNotContain("보관함 이름으로 사용할 POI를 선택해 주세요.");
    }
}
