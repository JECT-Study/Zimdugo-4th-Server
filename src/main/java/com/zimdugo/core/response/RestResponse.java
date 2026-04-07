package com.zimdugo.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse<T> {

    private final String code;
    private final int status;
    private final String message;
    private final T data;
    private final OffsetDateTime timestamp;
    private final Map<String, List<String>> errors;
    private final Map<String, List<String>> globalErrors;

    private RestResponse(
        BaseCode baseCode,
        T data,
        Map<String, List<String>> errors,
        Map<String, List<String>> globalErrors
    ) {
        this.code = baseCode.getCode();
        this.status = baseCode.getStatus().value();
        this.message = baseCode.getMessage();
        this.data = data;
        this.timestamp = OffsetDateTime.now();
        this.errors = errors;
        this.globalErrors = globalErrors;
    }

    public static <T> RestResponse<T> of(BaseCode code, T data) {
        return new RestResponse<>(code, data, null, null);
    }

    public static <T> RestResponse<T> ok(BaseCode code) {
        return new RestResponse<>(code, null, null, null);
    }

    public static <T> RestResponse<T> error(BaseCode code) {
        return new RestResponse<>(code, null, null, null);
    }

    public static <T> RestResponse<T> error(
        BaseCode code,
        Map<String, List<String>> errors,
        Map<String, List<String>> globalErrors
    ) {
        return new RestResponse<>(code, null, errors, globalErrors);
    }
}
