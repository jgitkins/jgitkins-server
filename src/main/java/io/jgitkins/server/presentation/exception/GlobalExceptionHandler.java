package io.jgitkins.server.presentation.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import io.jgitkins.server.application.common.exception.ApplicationException;
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
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, HttpServletRequest request) {

        HttpStatus status = ex.getStatus();

        log.info("Occur An Exception Message: [{}]", ex.getMessage());
        logExceptionDetails(status, request.getMethod(), ex);

        // ErrorResponse 객체 생성
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getErrorCode().getDefaultMessage(),
                ex.getMessage(),
                request.getRequestURI()
        );

        // 상태 코드와 함께 ErrorResponse 반환
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    private void logExceptionDetails(HttpStatus status, String method, ApplicationException ex){

        String errorCode = ex.getErrorCode().name();
        String exMessage = ex.getMessage();

        if (status.is5xxServerError()) {
            log.error( "[JGITKINS-SERVER-EX] status={}, code={}, method={}, message={}", status.value(), errorCode, method, exMessage, ex);
        } else {
            log.warn( "[JGITKINS-SERVER-EX] status={}, code={}, method={}, message={}", status.value(), errorCode, method, exMessage);
        }
    }
}
