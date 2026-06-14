package com.zimdugo.common.i18n;

import java.util.List;

public interface ContentLanguagePolicy {

    SupportedLanguage resolveRequestedLanguage(String acceptLanguage);

    List<SupportedLanguage> requiredTranslationLanguages();

    boolean isRequiredTranslationLanguage(SupportedLanguage language);
}
