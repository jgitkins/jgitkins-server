package io.jgitkins.server.presentation.advice;

import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;
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

    // Application, Damain, Infrastructure
    @ExceptionHandler(JgitkinsException.class)
    public ResponseEntity<ApiResponse<Void>> handleJgitkinsException(JgitkinsException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = statusMapper.map(errorCode);
        String source = inferSource(errorCode);

        if (SOURCE_INFRASTRUCTURE.equals(source)) {
            log.error("Infrastructure exception errorCode=[{}], status=[{}], message=[{}]",
                    errorCode.getCode(), status, exception.getMessage(), exception);
        } else {
            log.warn("{} exception errorCode=[{}], status=[{}], message=[{}]",
                    source, errorCode.getCode(), status, exception.getMessage());
        }

        return buildResponse(errorCode, status, exception.getMessage(), source);
    }

    // Presentation (Spring MVC / Validation specific)
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handlePresentationException(Exception exception) {
        String message = extractValidationMessage(exception);
        log.warn("Presentation exception errorCode=[{}], message=[{}]", PresentationErrorCode.BAD_REQUEST.getCode(), message);
        return buildResponse(PresentationErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message, SOURCE_PRESENTATION);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException exception) {
        // 정의되지 않은 엔드포인트 요청에 대해 404 상태코드와 BAD_REQUEST 코드를 반환합니다.
        return buildResponse(PresentationErrorCode.BAD_REQUEST, HttpStatus.NOT_FOUND, exception.getMessage(), SOURCE_PRESENTATION);
    }

    // Others
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected exception", exception);
        return buildResponse(InfrastructureErrorCode.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                SOURCE_PRESENTATION);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode,
                                                            HttpStatus status,
                                                            String message,
                                                            String source) {
        String responseMessage = (message == null || message.isBlank()) ? errorCode.getDefaultMessage() : message;
        return ResponseEntity.status(status).body(ApiResponse.failure(errorCode, responseMessage, source));
    }

    private String extractValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
            if (fieldError != null && fieldError.getDefaultMessage() != null && !fieldError.getDefaultMessage().isBlank()) {
                return fieldError.getDefaultMessage();
            }
        }
        return exception.getMessage();
    }

    private String inferSource(ErrorCode errorCode) {
        if (errorCode instanceof DomainErrorCode) {
            return SOURCE_DOMAIN;
        }
        if (errorCode instanceof InfrastructureErrorCode) {
            return SOURCE_INFRASTRUCTURE;
        }
        if (errorCode instanceof ApplicationErrorCode) {
            return SOURCE_APPLICATION;
        }
        if (errorCode instanceof PresentationErrorCode) {
            return SOURCE_PRESENTATION;
        }
        return SOURCE_APPLICATION;
    }
}
