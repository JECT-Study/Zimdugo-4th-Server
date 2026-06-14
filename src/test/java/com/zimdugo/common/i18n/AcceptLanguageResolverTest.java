package com.zimdugo.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class AcceptLanguageResolverTest {

    private final AcceptLanguageResolver resolver = new AcceptLanguageResolver();

    @Test
    void resolvesHighestPrioritySupportedLanguage() {
        SupportedLanguage language = resolver.resolve("fr-FR, ja-JP;q=0.9, en-US;q=0.8");

        assertThat(language).isEqualTo(SupportedLanguage.JAPANESE);
    }

    @Test
    void resolvesChineseScriptAndRegion() {
        assertThat(resolver.resolve("zh-CN")).isEqualTo(SupportedLanguage.SIMPLIFIED_CHINESE);
        assertThat(resolver.resolve("zh-Hant")).isEqualTo(SupportedLanguage.TRADITIONAL_CHINESE);
        assertThat(resolver.resolve("zh-TW")).isEqualTo(SupportedLanguage.TRADITIONAL_CHINESE);
    }

    @Test
    void fallsBackToEnglishForMissingOrInvalidHeader() {
        assertThat(resolver.resolve(null)).isEqualTo(SupportedLanguage.ENGLISH);
        assertThat(resolver.resolve("invalid;q=oops")).isEqualTo(SupportedLanguage.ENGLISH);
    }
}
