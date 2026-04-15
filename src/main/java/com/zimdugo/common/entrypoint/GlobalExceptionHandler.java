package com.zimdugo.common.entrypoint;

import com.zimdugo.core.exception.CustomException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.ValidationError;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<RestResponse<Void>> handleExternalApiException(ExternalApiException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("ExternalApiException 발생: code={}, message={}", errorCode.code(), e.getMessage(), e);
        return ResponseEntity.status(errorCode.httpStatus())
            .body(RestResponse.error(errorCode));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<RestResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CustomException 발생: code={}, message={}", errorCode.code(), e.getMessage());
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
        List<ValidationError> validationErrors = e.getConstraintViolations().stream()
            .map(violation -> new ValidationError(
                extractLastPathSegment(violation.getPropertyPath().toString()),
                toValidationMessage(
                    violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
                )
            ))
            .toList();

        log.warn("ConstraintViolationException 발생: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
            .body(RestResponse.error(
                ErrorCode.BAD_REQUEST,
                validationErrors
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.httpStatus())
            .body(RestResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> new ValidationError(
                fieldError.getField(),
                toValidationMessage(fieldError.getCode())
            ))
            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        ex.getBindingResult().getGlobalErrors().stream()
            .map(objectError -> new ValidationError(
                "global",
                toValidationMessage(objectError.getCode())
            ))
            .forEach(validationErrors::add);

        log.warn("MethodArgumentNotValidException 발생");
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.httpStatus())
            .body(RestResponse.error(
                ErrorCode.BAD_REQUEST,
                validationErrors
            ));
    }

    private String extractLastPathSegment(String path) {
        if (path == null || path.isBlank()) {
            return "global";
        }

        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1);
        }
        return path;
    }

    private String toValidationMessage(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return "validation.invalid";
        }

        String snake = rawCode
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replace('-', '_')
            .toLowerCase();
        return "validation." + snake;
    }
}
