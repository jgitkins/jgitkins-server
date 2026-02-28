package io.jgitkins.server.presentation.advice;


import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.presentation.common.error.PresentationErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.presentation.advice.mapper.CompositeErrorHttpStatusMapper;
import io.jgitkins.server.presentation.common.ApiResponse;
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
        io.jgitkins.server.common.error.ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = mapToStatus(errorCode);
        log.warn("Jgitkins exception errorCode=[{}], status=[{}], message=[{}]",
                errorCode.getCode(),
                status,
                exception.getMessage(),
                exception);
        return buildResponse(LegacyErrorCodeBridge.from(errorCode),
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
        return buildResponse(LegacyErrorCodeBridge.from(requestErrorCode),
                HttpStatus.BAD_REQUEST,
                message,
                "presentation");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException exception) {
        return buildResponse(ErrorCode.BAD_REQUEST, HttpStatus.NOT_FOUND, exception.getMessage(), "presentation");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        log.error("Unexpected exception", exception);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                "presentation");
    }

    private HttpStatus mapToStatus(io.jgitkins.server.common.error.ErrorCode errorCode) {
        return statusMapper.map(errorCode);
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(io.jgitkins.server.common.error.ErrorCode errorCode,
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
            return PresentationErrorCode.REQ_VALIDATION_FAILED;
        }
        if (exception instanceof HttpMessageNotReadableException) {
            return PresentationErrorCode.REQ_MALFORMED_JSON;
        }
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return PresentationErrorCode.REQ_TYPE_MISMATCH;
        }
        return PresentationErrorCode.REQ_BAD_REQUEST;
    }

    private String inferSource(io.jgitkins.server.common.error.ErrorCode errorCode) {
        String code = errorCode.getCode();
        if (code.startsWith("DOM_")) {
            return "domain";
        }
        if (code.startsWith("INF_")) {
            return "infrastructure";
        }
        if (code.startsWith("APP_")) {
            return "application";
        }
        if (code.startsWith("REQ_")) {
            return "presentation";
        }
        return "application";
    }

}
