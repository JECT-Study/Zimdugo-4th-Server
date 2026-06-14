package com.zimdugo.common.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AcceptLanguageResolver {

    private static final Set<String> TRADITIONAL_CHINESE_REGIONS = Set.of("HK", "MO", "TW");

    public SupportedLanguage resolve(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return SupportedLanguage.ENGLISH;
        }

        try {
            return resolveRanges(Locale.LanguageRange.parse(acceptLanguage));
        } catch (IllegalArgumentException exception) {
            return SupportedLanguage.ENGLISH;
        }
    }

    private SupportedLanguage resolveRanges(List<Locale.LanguageRange> languageRanges) {
        return languageRanges.stream()
            .filter(range -> range.getWeight() > 0)
            .map(Locale.LanguageRange::getRange)
            .map(AcceptLanguageResolver::parseLanguage)
            .flatMap(Optional::stream)
            .findFirst()
            .orElse(SupportedLanguage.ENGLISH);
    }

    public static Optional<SupportedLanguage> parseLanguage(String languageRange) {
        if (languageRange == null || languageRange.isBlank() || "*".equals(languageRange)) {
            return Optional.empty();
        }

        Locale locale = Locale.forLanguageTag(languageRange.replace('_', '-'));
        return switch (locale.getLanguage()) {
            case "ko" -> Optional.of(SupportedLanguage.KOREAN);
            case "en" -> Optional.of(SupportedLanguage.ENGLISH);
            case "ja" -> Optional.of(SupportedLanguage.JAPANESE);
            case "zh" -> Optional.of(resolveChineseStatic(locale));
            default -> Optional.empty();
        };
    }

    private static SupportedLanguage resolveChineseStatic(Locale locale) {
        if ("Hant".equalsIgnoreCase(locale.getScript())
            || TRADITIONAL_CHINESE_REGIONS.contains(locale.getCountry().toUpperCase(Locale.ROOT))) {
            return SupportedLanguage.TRADITIONAL_CHINESE;
        }
        return SupportedLanguage.SIMPLIFIED_CHINESE;
    }
}
