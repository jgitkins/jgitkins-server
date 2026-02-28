package io.jgitkins.server.presentation.advice;


import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;
import io.jgitkins.server.presentation.advice.mapper.CompositeErrorHttpStatusMapper;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.error.PresentationErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final CompositeErrorHttpStatusMapper statusMapper;

    @ExceptionHandler(JgitkinsException.class)
    public ResponseEntity<ApiResponse<Void>> handleJgitkinsException(JgitkinsException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = mapToStatus(errorCode);
        log.warn("Jgitkins exception errorCode=[{}], status=[{}], message=[{}]",
                errorCode.getCode(),
                status,
                exception.getMessage(),
                exception);
        return buildResponse(errorCode,
                status,
                exception.getMessage(),
                inferSource(errorCode));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        PresentationErrorCode requestErrorCode = mapPresentationErrorCode(exception);
        String message = extractValidationMessage(exception);
        log.warn("Bad request exception errorCode=[{}], message=[{}]", requestErrorCode.getCode(), message, exception);
        return buildResponse(requestErrorCode,
                HttpStatus.BAD_REQUEST,
                message,
                "presentation");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException exception) {
        return buildResponse(PresentationErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, exception.getMessage(), "presentation");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unexpected exception", exception);
        return buildResponse(ApplicationErrorCode.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApplicationErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                "presentation");
    }

    private HttpStatus mapToStatus(ErrorCode errorCode) {
        return statusMapper.map(errorCode);
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

    private PresentationErrorCode mapPresentationErrorCode(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException
                || exception instanceof ConstraintViolationException) {
            return PresentationErrorCode.VALIDATION_FAILED;
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return PresentationErrorCode.MALFORMED_JSON;
        }
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return PresentationErrorCode.TYPE_MISMATCH;
        }
        return PresentationErrorCode.BAD_REQUEST;
    }

    private String inferSource(ErrorCode errorCode) {
        if (errorCode instanceof DomainErrorCode) {
            return "domain";
        }
        if (errorCode instanceof InfrastructureErrorCode) {
            return "infrastructure";
        }
        if (errorCode instanceof ApplicationErrorCode) {
            return "application";
        }
        if (errorCode instanceof PresentationErrorCode) {
            return "presentation";
        }
        return "application";
    }

}
