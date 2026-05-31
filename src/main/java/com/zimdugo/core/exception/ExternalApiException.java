package com.zimdugo.core.exception;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(String message) {
        this(message, null);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_API_ERROR, message, cause);
    }
}
