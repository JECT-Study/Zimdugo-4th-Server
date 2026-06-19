package com.zimdugo.common.exception;

import com.zimdugo.common.filter.RequestTraceFilter;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String REQUEST_FIELD = "request";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RestResponse<Void>> handleBusinessException(
        BusinessException ex,
        HttpServletRequest request
    ) {
        ErrorCode errorCode = ex.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error(
                "서버 오류가 발생했습니다. code={}, method={}, path={}",
                errorCode.getCode(),
                request.getMethod(),
                request.getRequestURI(),
                ex
            );
            return errorResponse(errorCode, errorCode.getMessage(), null, request);
        }
        log.debug(
            "비즈니스 예외가 발생했습니다. code={}, method={}, path={}",
            errorCode.getCode(),
            request.getMethod(),
            request.getRequestURI()
        );
        return errorResponse(errorCode, ex.getMessage(), null, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return errorResponse(
            ErrorCode.VALIDATION_FAILED,
            ErrorCode.VALIDATION_FAILED.getMessage(),
            toValidationErrors(
                ex.getBindingResult().getFieldErrors(),
                ex.getBindingResult().getGlobalErrors()
            ),
            request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
        HandlerMethodValidationException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        return errorResponse(
            ErrorCode.VALIDATION_FAILED,
            ErrorCode.VALIDATION_FAILED.getMessage(),
            toValidationErrors(ex),
            request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        HttpServletRequest servletRequest = servletRequest(request);
        log.warn(
            "요청 본문 형식이 올바르지 않습니다. method={}, path={}",
            servletRequest.getMethod(),
            servletRequest.getRequestURI()
        );
        return errorResponse(
            ErrorCode.INVALID_JSON_FORMAT,
            ErrorCode.INVALID_JSON_FORMAT.getMessage(),
            null,
            request
        );
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        TypeMismatchException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        HttpServletRequest servletRequest = servletRequest(request);
        log.warn(
            "요청 파라미터 형식이 올바르지 않습니다. method={}, path={}, field={}",
            servletRequest.getMethod(),
            servletRequest.getRequestURI(),
            ex.getPropertyName() == null ? REQUEST_FIELD : ex.getPropertyName()
        );
        ValidationError validationError = ValidationError.of(
            ex.getPropertyName() == null ? REQUEST_FIELD : ex.getPropertyName(),
            ErrorCode.INVALID_PARAMETER_FORMAT.getMessage(),
            ex.getValue()
        );
        return errorResponse(
            ErrorCode.INVALID_PARAMETER_FORMAT,
            ErrorCode.INVALID_PARAMETER_FORMAT.getMessage(),
            List.of(validationError),
            request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestResponse<Void>> handleConstraintViolationException(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<ValidationError> validationErrors = ex.getConstraintViolations().stream()
            .map(this::toValidationError)
            .toList();
        return errorResponse(
            ErrorCode.VALIDATION_FAILED,
            ErrorCode.VALIDATION_FAILED.getMessage(),
            validationErrors,
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleUnhandledException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error("처리되지 않은 예외가 발생했습니다. method={}, path={}", request.getMethod(), request.getRequestURI(), ex);
        return errorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
            null,
            request
        );
    }

    private List<ValidationError> toValidationErrors(
        List<FieldError> fieldErrors,
        List<ObjectError> globalErrors
    ) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            validationErrors.add(ValidationError.of(
                fieldError.getField(),
                resolveMessage(fieldError),
                fieldError.getRejectedValue()
            ));
        }
        for (ObjectError globalError : globalErrors) {
            validationErrors.add(ValidationError.of(
                REQUEST_FIELD,
                resolveMessage(globalError),
                null
            ));
        }
        return validationErrors;
    }

    private List<ValidationError> toValidationErrors(HandlerMethodValidationException ex) {
        List<ValidationError> validationErrors = new ArrayList<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                validationErrors.addAll(toValidationErrors(
                    parameterErrors.getFieldErrors(),
                    parameterErrors.getGlobalErrors()
                ));
                continue;
            }

            String parameterName = resolveParameterName(result);
            for (MessageSourceResolvable resolvableError : result.getResolvableErrors()) {
                validationErrors.add(ValidationError.of(
                    parameterName,
                    resolveMessage(resolvableError),
                    result.getArgument()
                ));
            }
        }

        for (MessageSourceResolvable resolvableError : ex.getCrossParameterValidationResults()) {
            validationErrors.add(ValidationError.of(
                REQUEST_FIELD,
                resolveMessage(resolvableError),
                null
            ));
        }
        return validationErrors;
    }

    private String resolveParameterName(ParameterValidationResult result) {
        String parameterName = result.getMethodParameter().getParameterName();
        if (parameterName == null || parameterName.isBlank()) {
            return REQUEST_FIELD;
        }
        return parameterName;
    }

    private String resolveMessage(MessageSourceResolvable messageSourceResolvable) {
        if (messageSourceResolvable.getDefaultMessage() != null
            && !messageSourceResolvable.getDefaultMessage().isBlank()) {
            return messageSourceResolvable.getDefaultMessage();
        }
        String[] codes = messageSourceResolvable.getCodes();
        if (codes != null && codes.length > 0) {
            return codes[0];
        }
        return ErrorCode.VALIDATION_FAILED.getMessage();
    }

    private ValidationError toValidationError(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath() == null ? REQUEST_FIELD : violation.getPropertyPath().toString();
        String field = extractLastPathSegment(path);
        return ValidationError.of(field, violation.getMessage(), violation.getInvalidValue());
    }

    private String extractLastPathSegment(String path) {
        if (path == null || path.isBlank()) {
            return REQUEST_FIELD;
        }
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < path.length() - 1) {
            return path.substring(lastDotIndex + 1);
        }
        return path;
    }

    private ResponseEntity<RestResponse<Void>> errorResponse(
        ErrorCode code,
        String message,
        List<ValidationError> validationErrors,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(code.getStatus())
            .body(RestResponse.error(
                code,
                message,
                validationErrors,
                request.getRequestURI(),
                resolveTraceId(request)
            ));
    }

    private ResponseEntity<Object> errorResponse(
        ErrorCode code,
        String message,
        List<ValidationError> validationErrors,
        WebRequest request
    ) {
        return ResponseEntity.status(code.getStatus())
            .body(RestResponse.error(
                code,
                message,
                validationErrors,
                resolvePath(request),
                resolveTraceId(request)
            ));
    }

    private String resolvePath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return null;
    }

    private String resolveTraceId(HttpServletRequest request) {
        Object traceIdAttribute = request.getAttribute(RequestTraceFilter.TRACE_ID_ATTRIBUTE);
        if (traceIdAttribute instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        String traceId = request.getHeader(RequestTraceFilter.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            return null;
        }
        return traceId;
    }

    private String resolveTraceId(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return resolveTraceId(servletWebRequest.getRequest());
        }
        String traceId = request.getHeader(RequestTraceFilter.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            return null;
        }
        return traceId;
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }
}
