package com.zimdugo.core.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode implements BaseCode {
    OK("S200", "common.ok", HttpStatus.OK);

    private final String code;
    private final String message;
    private final HttpStatus status;

    SuccessCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

}
