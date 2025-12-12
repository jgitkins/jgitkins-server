package io.jgitkins.server.presentation.advice;


import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ApplicationException;
import io.jgitkins.server.presentation.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * ApplicationException을 상속하는 예외 처리
     */

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplication(ApplicationException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        HttpStatus status = mapToStatus(errorCode);
        log.warn("Application exception errorCode: [{}] || message: [{}] || stack: [{}], status: [{}] ", errorCode.getCode(), exception.getMessage(), exception,  status);
        return buildResponse(errorCode, status, exception.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolations(Exception exception) {
        log.warn("Constraint Violation: {}", exception.getMessage(), exception);
        return buildResponse(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, exception.getMessage());
    }


    private HttpStatus mapToStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;

            case REPOSITORY_NOT_FOUND,
                 SOURCE_BRANCH_NOT_FOUND,
                 BRANCH_NOT_FOUND,
                 COMMIT_NOT_FOUND,
                 COMMIT_TREE_NOT_FOUND,
                 RUNNER_NOT_FOUND,
                 ORGANIZE_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case REPOSITORY_ALREADY_EXISTS,
                 BRANCH_ALREADY_EXISTS,
                 ORGANIZE_ALREADY_EXISTS -> HttpStatus.CONFLICT;

            case INTERNAL_SERVER_ERROR,
                 REPOSITORY_CREATE_FAILED,
                 BRANCH_CREATE_FAILED,
                 BRANCH_DELETE_FAILED,
                 BRANCH_LOAD_FAILED,
                 HEAD_POINT_FAILED,
                 COMMIT_LOAD_FAILED,
                 COMMIT_CREATE_FAILED,
                 COMMIT_TREE_LOAD_FAILED,
                 FILE_LOAD_FAILED,
                 RUNNER_DELETE_FAILED,
                 RUNNER_ACTIVATION_FAILED,
                 REPOSITORY_DELETE_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }


    private ResponseEntity<ApiResponse<Void>> buildResponse(ErrorCode errorCode, HttpStatus status, String message) {
        String responseMessage = (message == null || message.isBlank()) ? errorCode.getDefaultMessage() : message;
        return ResponseEntity.status(status).body(ApiResponse.failure(errorCode, responseMessage));
    }

}
