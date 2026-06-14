package com.zimdugo.locker.infrastructure.localization;

import com.zimdugo.common.i18n.SupportedLanguage;

public record LocalizedLockerContent(
    String name,
    String roadAddress,
    String detailInfo,
    SupportedLanguage language
) {
}
