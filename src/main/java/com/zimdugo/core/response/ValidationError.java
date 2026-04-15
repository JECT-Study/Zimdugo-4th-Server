package com.zimdugo.core.response;

public record ValidationError(
    String field,
    String message
) {
}
