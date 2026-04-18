package com.zimdugo.core.exception;

import com.zimdugo.core.response.BaseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseCode {
    BAD_REQUEST("C400", "common.bad_request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("C401", "common.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("C403", "common.forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("C404", "common.not_found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("C500", "common.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR),


    REFRESH_TOKEN_NOT_FOUND("A4001", "auth.refresh_token_not_found", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("A4002", "auth.invalid_refresh_token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_MISMATCH("A4003", "auth.refresh_token_mismatch", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REVOKED("A4004", "auth.refresh_token_revoked", HttpStatus.BAD_REQUEST),


    EXTERNAL_API_ERROR("E5021", "external.api_error", HttpStatus.BAD_GATEWAY),


    USER_NOT_FOUND("U4041", "user.not_found", HttpStatus.NOT_FOUND),
    USER_ALREADY_WITHDRAWN("U4002", "user.already_withdrawn", HttpStatus.BAD_REQUEST),


    UNSUPPORTED_SOCIAL_LOGIN("A4005", "auth.unsupported_social_login", HttpStatus.BAD_REQUEST),
    AUTHENTICATED_USER_NOT_FOUND("A4011", "auth.authenticated_user_not_found", HttpStatus.UNAUTHORIZED);

    @Getter
    private final String code;
    @Getter
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    public HttpStatus getStatus() {
        return httpStatus;
    }
}
