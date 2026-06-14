package com.zimdugo.common.i18n;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SupportedLanguageConverter implements AttributeConverter<SupportedLanguage, String> {

    @Override
    public String convertToDatabaseColumn(SupportedLanguage language) {
        return language == null ? null : language.languageTag();
    }

    @Override
    public SupportedLanguage convertToEntityAttribute(String languageTag) {
        if (languageTag == null) {
            return null;
        }
        return SupportedLanguage.fromTag(languageTag)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LANGUAGE_TAG));
    }
}
