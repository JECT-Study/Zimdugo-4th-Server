package com.zimdugo.locker.infrastructure.localization;

import com.zimdugo.common.i18n.SupportedLanguage;

public record LocalizedPlaceContent(
    String name,
    String roadAddress,
    SupportedLanguage language
) {
}
