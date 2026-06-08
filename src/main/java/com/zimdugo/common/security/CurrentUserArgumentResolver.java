package com.zimdugo.common.security;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.security.Principal;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && (Long.class.equals(parameter.getParameterType()) || long.class.equals(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Principal principal = webRequest.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }

        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.AUTHENTICATED_USER_NOT_FOUND);
        }
    }
}
