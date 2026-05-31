package com.zimdugo.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.core.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse<T> {

    private final String code;
    private final String message;
    private final int status;
    private final T data;
    private final OffsetDateTime timestamp;
    private final String path;
    private final String traceId;
    private final List<ValidationError> validationErrors;

    private RestResponse(
        BaseCode baseCode,
        String message,
        T data,
        ErrorMeta errorMeta
    ) {
        this.code = baseCode.getCode();
        this.message = message;
        this.status = baseCode.getStatusCode();
        this.data = data;
        this.timestamp = nowUtc();
        this.path = errorMeta.path();
        this.traceId = errorMeta.traceId();
        this.validationErrors = errorMeta.validationErrors();
    }

    public static <T> RestResponse<T> of(SuccessCode code, T data) {
        return success(code, data);
    }

    public static RestResponse<Void> ok(SuccessCode code) {
        return success(code, null);
    }

    public static RestResponse<Void> error(ErrorCode code) {
        return failure(code, code.getMessage(), ErrorMeta.empty());
    }

    public static RestResponse<Void> error(ErrorCode code, List<ValidationError> validationErrors) {
        return failure(
            code,
            code.getMessage(),
            new ErrorMeta(null, null, validationErrors)
        );
    }

    public static RestResponse<Void> error(
        ErrorCode code,
        String message,
        String path,
        String traceId
    ) {
        return failure(code, message, new ErrorMeta(path, traceId, null));
    }

    public static RestResponse<Void> error(
        ErrorCode code,
        List<ValidationError> validationErrors,
        String path,
        String traceId
    ) {
        return failure(
            code,
            code.getMessage(),
            new ErrorMeta(path, traceId, validationErrors)
        );
    }

    public static RestResponse<Void> error(
        ErrorCode code,
        String message,
        List<ValidationError> validationErrors,
        String path,
        String traceId
    ) {
        return failure(code, message, new ErrorMeta(path, traceId, validationErrors));
    }

    private static <T> RestResponse<T> success(SuccessCode code, T data) {
        return new RestResponse<>(code, code.getMessage(), data, ErrorMeta.empty());
    }

    private static RestResponse<Void> failure(
        ErrorCode code,
        String message,
        ErrorMeta errorMeta
    ) {
        return new RestResponse<>(code, message, null, errorMeta);
    }

    private static OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record ErrorMeta(
        String path,
        String traceId,
        List<ValidationError> validationErrors
    ) {
        private static ErrorMeta empty() {
            return new ErrorMeta(null, null, null);
        }
    }
}
