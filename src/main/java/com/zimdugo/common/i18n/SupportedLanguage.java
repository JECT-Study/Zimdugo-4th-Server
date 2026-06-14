package com.zimdugo.common.i18n;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SupportedLanguage {

    KOREAN("ko"),
    ENGLISH("en"),
    JAPANESE("ja"),
    SIMPLIFIED_CHINESE("zh-Hans"),
    TRADITIONAL_CHINESE("zh-Hant");

    private static final Map<String, SupportedLanguage> LANGUAGE_MAP =
        Arrays.stream(values()).collect(Collectors.toMap(
            language -> language.languageTag().toLowerCase(),
            Function.identity()
        ));

    private final String languageTag;

    SupportedLanguage(String languageTag) {
        this.languageTag = languageTag;
    }

    @JsonValue
    public String languageTag() {
        return languageTag;
    }

    public static List<SupportedLanguage> all() {
        return List.of(values());
    }

    @JsonCreator
    public static SupportedLanguage fromJson(String languageTag) {
        return AcceptLanguageResolver.parseLanguage(languageTag)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LANGUAGE_TAG));
    }

    public static Optional<SupportedLanguage> fromTag(String languageTag) {
        if (languageTag == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(LANGUAGE_MAP.get(languageTag.toLowerCase()));
    }
}
