package com.zimdugo.core.response;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    String getCode();

    HttpStatus getStatus();

    String getMessage();
}
