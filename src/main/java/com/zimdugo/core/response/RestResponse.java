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
    private final List<ValidationError> validationErrors;

    private RestResponse(BaseCode baseCode, T data, List<ValidationError> validationErrors) {
        this.code = baseCode.getCode();
        this.message = baseCode.getMessage();
        this.status = baseCode.getStatus().value();
        this.data = data;
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.validationErrors = validationErrors;
    }

    public static <T> RestResponse<T> ok(SuccessCode code, T data) {
        return success(code, data);
    }

    public static RestResponse<Void> ok(SuccessCode code) {
        return success(code, null);
    }

    public static RestResponse<Void> error(ErrorCode code) {
        return failure(code, null);
    }

    public static RestResponse<Void> error(ErrorCode code, List<ValidationError> validationErrors) {
        return failure(code, validationErrors);
    }

    private static <T> RestResponse<T> success(SuccessCode code, T data) {
        return new RestResponse<>(code, data, null);
    }

    private static RestResponse<Void> failure(
        ErrorCode code,
        List<ValidationError> validationErrors
    ) {
        return new RestResponse<>(code, null, validationErrors);
    }
}
