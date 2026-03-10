package io.jgitkins.server.presentation.advice;

import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.exception.DomainException;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;
import io.jgitkins.server.infrastructure.exception.InfrastructureException;
import io.jgitkins.server.presentation.advice.mapper.CompositeErrorHttpStatusMapper;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.error.PresentationErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String SOURCE_PRESENTATION = "presentation";
    private static final String SOURCE_APPLICATION = "application";
    private static final String SOURCE_DOMAIN = "domain";
    private static final String SOURCE_INFRASTRUCTURE = "infrastructure";

    private final CompositeErrorHttpStatusMapper statusMapper;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = statusMapper.map(errorCode);
        log.warn("Domain exception errorCode=[{}], status=[{}], message=[{}]",
                errorCode.getCode(), status, ex.getMessage());
        return buildResponse(errorCode, status, ex.getMessage(), SOURCE_DOMAIN);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = statusMapper.map(errorCode);
        log.warn("Application exception errorCode=[{}], status=[{}], message=[{}]",
                errorCode.getCode(), status, ex.getMessage());
        return buildResponse(errorCode, status, ex.getMessage(), SOURCE_APPLICATION);
    }

    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInfrastructureException(InfrastructureException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = statusMapper.map(errorCode);
        log.error("Infrastructure exception errorCode=[{}], status=[{}], message=[{}]",
                errorCode.getCode(), status, ex.getMessage(), ex);
        return buildResponse(errorCode, status, ex.getMessage(), SOURCE_INFRASTRUCTURE);
    }

    // fallback: JgitkinsException 직접 사용 지점 (점진적으로 줄여나갈 대상)
    @ExceptionHandler(JgitkinsException.class)
    public ResponseEntity<ApiResponse<Void>> handleJgitkinsException(JgitkinsException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = statusMapper.map(errorCode);
        String source = inferSourceFallback(errorCode);

        if (SOURCE_INFRASTRUCTURE.equals(source)) {
            log.error("Infrastructure exception (fallback) errorCode=[{}], status=[{}], message=[{}]",
                    errorCode.getCode(), status, ex.getMessage(), ex);
        } else {
            log.warn("{} exception (fallback) errorCode=[{}], status=[{}], message=[{}]",
                    source, errorCode.getCode(), status, ex.getMessage());
        }
        return buildResponse(errorCode, status, ex.getMessage(), source);
    }

    // Presentation (Spring MVC / Validation specific)
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handlePresentationException(Exception ex) {
        String message = extractValidationMessage(ex);
        log.warn("Presentation exception errorCode=[{}], message=[{}]",
                PresentationErrorCode.BAD_REQUEST.getCode(), message);
        return buildResponse(PresentationErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, SOURCE_PRESENTATION);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException ex) {
        return buildResponse(PresentationErrorCode.BAD_REQUEST, HttpStatus.NOT_FOUND, ex.getMessage(),
                SOURCE_PRESENTATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected exception", ex);
        return buildResponse(InfrastructureErrorCode.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                SOURCE_INFRASTRUCTURE);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode,
            HttpStatus status,
            String message,
            String source) {
        String responseMessage = (message == null || message.isBlank())
                ? errorCode.getDefaultMessage()
                : message;
        return ResponseEntity.status(status).body(ApiResponse.failure(errorCode, responseMessage, source));
    }

    private String extractValidationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            FieldError fieldError = manve.getBindingResult().getFieldError();
            if (fieldError != null
                    && fieldError.getDefaultMessage() != null
                    && !fieldError.getDefaultMessage().isBlank()) {
                return fieldError.getDefaultMessage();
            }
        }
        return ex.getMessage();
    }

    /**
     * fallback용: JgitkinsException을 직접 throw하는 레거시 코드에서만 사용.
     * 계층별 예외로 치환이 완료되면 이 메서드는 제거한다.
     */
    private String inferSourceFallback(ErrorCode errorCode) {
        String className = errorCode.getClass().getSimpleName();
        if (className.startsWith("Domain"))
            return SOURCE_DOMAIN;
        if (className.startsWith("Infrastructure"))
            return SOURCE_INFRASTRUCTURE;
        if (className.startsWith("Application"))
            return SOURCE_APPLICATION;
        if (className.startsWith("Presentation"))
            return SOURCE_PRESENTATION;
        return SOURCE_APPLICATION;
    }
}
