package com.zimdugo.core.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("C400", "bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("C401", "unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("C403", "forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("C404", "resource not found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("C500", "internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    REFRESH_TOKEN_NOT_FOUND("A4001", "refresh token not found", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("A4002", "invalid refresh token", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_MISMATCH("A4003", "refresh token mismatch", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REVOKED("A4004", "refresh token revoked", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("U4041", "user not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_WITHDRAWN("U4002", "user already withdrawn", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_SOCIAL_LOGIN("A4005", "unsupported social login provider", HttpStatus.BAD_REQUEST),
    AUTHENTICATED_USER_NOT_FOUND("A4011", "authenticated user not found", HttpStatus.UNAUTHORIZED);

    private final String code;
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
}
