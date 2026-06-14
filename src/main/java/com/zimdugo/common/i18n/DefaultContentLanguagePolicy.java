package com.zimdugo.common.i18n;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultContentLanguagePolicy implements ContentLanguagePolicy {

    private final AcceptLanguageResolver acceptLanguageResolver;

    @Override
    public SupportedLanguage resolveRequestedLanguage(String acceptLanguage) {
        return acceptLanguageResolver.resolve(acceptLanguage);
    }

    @Override
    public List<SupportedLanguage> requiredTranslationLanguages() {
        return SupportedLanguage.all();
    }

    @Override
    public boolean isRequiredTranslationLanguage(SupportedLanguage language) {
        return language != null;
    }
}
