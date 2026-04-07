package com.zimdugo.common.entrypoint;

import com.zimdugo.core.exception.CustomException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.core.response.RestResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<RestResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        if (e instanceof ExternalApiException) {
            log.error("ExternalApiException 발생: code={}, message={}", errorCode.code(), e.getMessage(), e);
            return ResponseEntity.status(errorCode.httpStatus())
                .body(RestResponse.error(errorCode));
        } else {
            log.warn("CustomException 발생: code={}, message={}", errorCode.code(), e.getMessage());
        }
        return ResponseEntity.status(errorCode.httpStatus())
            .body(RestResponse.error(errorCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException 발생: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
            .body(RestResponse.error(ErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(violation -> addError(
            errors,
            violation.getPropertyPath().toString(),
            violation.getMessage()
        ));

        log.warn("ConstraintViolationException 발생: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
            .body(RestResponse.error(
                ErrorCode.BAD_REQUEST,
                errors,
                null
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus())
            .body(RestResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        Map<String, List<String>> globalErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fieldError -> addError(
                errors,
                fieldError.getField(),
                fieldError.getDefaultMessage()
            ));
        ex.getBindingResult().getGlobalErrors()
            .forEach(objectError -> addError(
                globalErrors,
                objectError.getObjectName(),
                objectError.getDefaultMessage()
            ));

        log.warn("MethodArgumentNotValidException 발생");
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
            .body(RestResponse.error(
                ErrorCode.BAD_REQUEST,
                errors,
                globalErrors.isEmpty() ? null : globalErrors
            ));
    }

    private void addError(Map<String, List<String>> target, String key, String message) {
        target.computeIfAbsent(key, ignored -> new ArrayList<>()).add(message);
    }
}
