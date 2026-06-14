package com.zimdugo.admin.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentLanguageTest {

    @Test
    @DisplayName("지원 언어 태그는 시스템 언어 태그로 정규화한다")
    void normalizesSupportedLanguageTag() {
        assertThat(DocumentLanguage.normalize("ZH_tw")).isEqualTo("zh-Hant");
    }

    @Test
    @DisplayName("지원하지 않는 언어 태그는 요청 오류로 처리한다")
    void rejectsInvalidLanguageTag() {
        assertThatThrownBy(() -> DocumentLanguage.normalize("fr"))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LANGUAGE_TAG)
            );
    }
}
