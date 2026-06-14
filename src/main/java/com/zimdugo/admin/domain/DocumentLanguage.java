package com.zimdugo.admin.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.common.i18n.SupportedLanguage;

public final class DocumentLanguage {

    private DocumentLanguage() {
    }

    public static String normalize(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_LANGUAGE_TAG);
        }
        return SupportedLanguage.fromJson(languageTag.trim()).languageTag();
    }
}
