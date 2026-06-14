package com.zimdugo.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentLanguagePolicyTest {

    private final ContentLanguagePolicy policy = new DefaultContentLanguagePolicy(new AcceptLanguageResolver());

    @Test
    void requiresEverySupportedLanguage() {
        assertThat(policy.requiredTranslationLanguages())
            .containsExactlyElementsOf(SupportedLanguage.all());
    }

    @Test
    void marksEverySupportedLanguageAsRequired() {
        assertThat(policy.isRequiredTranslationLanguage(SupportedLanguage.ENGLISH)).isTrue();
        assertThat(policy.isRequiredTranslationLanguage(SupportedLanguage.JAPANESE)).isTrue();
    }
}
