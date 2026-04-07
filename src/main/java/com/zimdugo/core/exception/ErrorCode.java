package com.zimdugo.core.exception;

import com.zimdugo.core.response.BaseCode;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseCode {
    BAD_REQUEST("C400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("C401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("C403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("C404", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("C500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),


    REFRESH_TOKEN_NOT_FOUND("A4001", "리프레시 토큰을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("A4002", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_MISMATCH("A4003", "리프레시 토큰이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REVOKED("A4004", "폐기된 리프레시 토큰입니다.", HttpStatus.BAD_REQUEST),


    EXTERNAL_API_ERROR("E5021", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),


    USER_NOT_FOUND("U4041", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_ALREADY_WITHDRAWN("U4002", "이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST),


    UNSUPPORTED_SOCIAL_LOGIN("A4005", "지원하지 않는 소셜 로그인 제공자입니다.", HttpStatus.BAD_REQUEST),
    AUTHENTICATED_USER_NOT_FOUND("A4011", "인증된 사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED);

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

    @Override
    public String getCode() {
        return code;
    }

    public String message() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}
