package com.zimdugo.common.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CurrentRequestLanguageTest {

    private final CurrentRequestLanguage currentRequestLanguage = new CurrentRequestLanguage(
        new AcceptLanguageResolver()
    );

    @AfterEach
    void clearRequestContext() {
        LocaleContextHolder.resetLocaleContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resolvesNextSupportedLanguageFromAcceptLanguageHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "fr-FR, ja-JP;q=0.9, en-US;q=0.8");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        LocaleContextHolder.setLocale(Locale.FRANCE);

        assertThat(currentRequestLanguage.resolve()).isEqualTo(SupportedLanguage.JAPANESE);
    }
}
