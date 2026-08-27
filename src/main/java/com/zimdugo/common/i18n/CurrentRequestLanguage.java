package com.zimdugo.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class CurrentRequestLanguage {

    private final AcceptLanguageResolver acceptLanguageResolver;

    public SupportedLanguage resolve() {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            return acceptLanguageResolver.resolve(request.getHeader("Accept-Language"));
        }
        return acceptLanguageResolver.resolve(LocaleContextHolder.getLocale().toLanguageTag());
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            return servletAttributes.getRequest();
        }
        return null;
    }
}
