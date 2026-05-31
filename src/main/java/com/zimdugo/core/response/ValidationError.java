package com.zimdugo.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationError(
    String field,
    String message,
    Object rejectedValue
) {
    public static ValidationError of(String field, String message) {
        return new ValidationError(field, message, null);
    }

    public static ValidationError of(String field, String message, Object rejectedValue) {
        return new ValidationError(field, message, rejectedValue);
    }
}
