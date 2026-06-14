package com.zimdugo.common.i18n;

import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentRequestLanguage {

    public SupportedLanguage resolve() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale == null) {
            return SupportedLanguage.ENGLISH;
        }
        return AcceptLanguageResolver.parseLanguage(locale.toLanguageTag())
            .orElse(SupportedLanguage.ENGLISH);
    }
}
