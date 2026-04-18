package com.zimdugo.core.exception;

public class ExternalApiException extends CustomException {

    public ExternalApiException(String message) {
        super(ErrorCode.EXTERNAL_API_ERROR, message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_API_ERROR, message, cause);
    }
}
