package com.zimdugo.core.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ErrorResponse {

    private ErrorResponse() {
    }

    public static Map<String, Object> of(ErrorCode errorCode, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", errorCode.httpStatus().value());
        body.put("code", errorCode.code());
        body.put("message", errorCode.message());
        body.put("path", path);
        body.put("timestamp", OffsetDateTime.now().toString());
        return body;
    }

    public static Map<String, Object> of(ErrorCode errorCode, String path, String overrideMessage) {
        Map<String, Object> body = of(errorCode, path);
        body.put("message", overrideMessage);
        return body;
    }
}
