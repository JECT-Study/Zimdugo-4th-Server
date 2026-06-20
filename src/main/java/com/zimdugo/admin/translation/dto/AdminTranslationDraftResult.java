package com.zimdugo.admin.translation.dto;

import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;

public record AdminTranslationDraftResult(
    List<Translation> translations
) {
    public record Translation(
        SupportedLanguage language,
        String name,
        String roadAddress,
        String detailInfo,
        List<String> aliases
    ) {
    }
}
