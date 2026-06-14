package com.zimdugo.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class SupportedLanguageConverterTest {

    private final SupportedLanguageConverter converter = new SupportedLanguageConverter();

    @Test
    void storesBcp47LanguageTag() {
        assertThat(converter.convertToDatabaseColumn(SupportedLanguage.SIMPLIFIED_CHINESE))
            .isEqualTo("zh-Hans");
    }

    @Test
    void restoresSupportedLanguageIgnoringTagCase() {
        assertThat(converter.convertToEntityAttribute("ZH-hant"))
            .isEqualTo(SupportedLanguage.TRADITIONAL_CHINESE);
    }

    @Test
    void rejectsUnsupportedLanguageTag() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("fr"))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LANGUAGE_TAG)
            );
    }
}
