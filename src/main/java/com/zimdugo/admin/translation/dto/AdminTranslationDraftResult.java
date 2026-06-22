package com.zimdugo.admin.translation.dto;

import com.zimdugo.common.i18n.SupportedLanguage;
import java.util.List;

public record AdminTranslationDraftResult(
    List<PlaceTranslation> placeTranslations,
    List<LockerTranslation> lockerTranslations
) {
    public PlaceTranslation placeTranslationFor(String languageTag) {
        if (languageTag == null || placeTranslations == null) {
            return null;
        }
        return placeTranslations.stream()
            .filter(item -> languageTag.equals(item.language().languageTag()))
            .findFirst()
            .orElse(null);
    }

    public LockerTranslation lockerTranslationFor(String languageTag) {
        if (languageTag == null || lockerTranslations == null) {
            return null;
        }
        return lockerTranslations.stream()
            .filter(item -> languageTag.equals(item.language().languageTag()))
            .findFirst()
            .orElse(null);
    }

    public record PlaceTranslation(
        SupportedLanguage language,
        String name,
        String roadAddress,
        List<String> aliases
    ) {
    }

    public record LockerTranslation(
        SupportedLanguage language,
        String name,
        String roadAddress,
        String detailInfo,
        List<String> aliases
    ) {
    }
}
