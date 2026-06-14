package com.zimdugo.core.exception;

import com.zimdugo.core.response.BaseCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements BaseCode {
    // 공통 (COMMON)
    BAD_REQUEST("COMMON-400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED("COMMON-400-1", "요청 값 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_JSON_FORMAT("COMMON-400-2", "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해 주세요.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON-401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON-403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("COMMON-404", "요청한 대상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER_FORMAT("COMMON-400-3", "요청 파라미터 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("COMMON-500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 인증 (AUTH)
    REFRESH_TOKEN_NOT_FOUND("AUTH-400-1", "리프레시 토큰이 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH-400-2", "리프레시 토큰이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_MISMATCH("AUTH-400-3", "리프레시 토큰이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_REVOKED("AUTH-400-4", "리프레시 토큰이 폐기되었습니다.", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_SOCIAL_LOGIN("AUTH-400-5", "지원하지 않는 소셜 로그인 제공자입니다.", HttpStatus.BAD_REQUEST),
    OAUTH2_INVALID_USER_INFO("AUTH-400-6", "소셜 로그인 사용자 정보를 가져올 수 없습니다.", HttpStatus.BAD_REQUEST),
    AUTHENTICATED_USER_NOT_FOUND("AUTH-401-1", "인증된 사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED),

    // 외부 API (EXTERNAL)
    EXTERNAL_API_ERROR("EXTERNAL-502-1", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),

    // 사용자 (USER)
    USER_NOT_FOUND("USER-404-1", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_ALREADY_WITHDRAWN("USER-400-2", "이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST),

    // 보관함 (LOCKER)
    LOCKER_NOT_FOUND("LOCKER-404-1", "보관함을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PLACE_NOT_FOUND("LOCKER-404-2", "장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LOCKER_REPORT_NOT_FOUND("LOCKER-404-3", "보관함 제보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_LOCATION_RANGE("LOCKER-400-1", "좌표 범위가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_LOCKER_SIZE_TYPE("LOCKER-400-3", "보관함 크기 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INDEX_SYNC_FAILED("LOCKER-500-1", "검색 인덱스 동기화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 이미지 (IMAGE)
    UNSUPPORTED_IMAGE_TYPE("IMAGE-400-1", "지원하지 않는 이미지 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_EXTENSION("IMAGE-400-2", "이미지 파일 확장자가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

    @Getter
    private final String code;
    @Getter
    private final String message;
    @Getter
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}
