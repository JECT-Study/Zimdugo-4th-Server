package com.zimdugo.push.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.Arrays;

public enum PushLocale {
    KO("ko"),
    EN("en"),
    JA("ja"),
    ZH_HANS("zh-Hans"),
    ZH_HANT("zh-Hant");

    private final String languageTag;

    PushLocale(String languageTag) {
        this.languageTag = languageTag;
    }

    public static PushLocale from(String languageTag) {
        return Arrays.stream(values())
            .filter(locale -> locale.languageTag.equals(languageTag))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LANGUAGE_TAG));
    }

    public String languageTag() {
        return languageTag;
    }
}
