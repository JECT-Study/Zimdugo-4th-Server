package com.zimdugo.core.response;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    String getCode();

    String getMessage();

    HttpStatus getStatus();
}
