package io.jgitkins.server.presentation.advice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ApplicationException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns401WithStandardErrorPayload_whenUnauthorized() throws Exception {
        mockMvc.perform(get("/test-errors/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("token missing"));
    }

    @Test
    void returns403WithStandardErrorPayload_whenForbidden() throws Exception {
        mockMvc.perform(get("/test-errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("not allowed"));
    }

    @Test
    void returns404WithStandardErrorPayload_whenResourceNotFound() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("REPOSITORY_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("repo missing"));
    }

    @RestController
    static class ExceptionThrowingController {

        @GetMapping("/test-errors/unauthorized")
        public ResponseEntity<Void> unauthorized() {
            throw new TestApplicationException(ErrorCode.UNAUTHORIZED, "token missing");
        }

        @GetMapping("/test-errors/forbidden")
        public ResponseEntity<Void> forbidden() {
            throw new TestApplicationException(ErrorCode.FORBIDDEN, "not allowed");
        }

        @GetMapping("/test-errors/not-found")
        public ResponseEntity<Void> notFound() {
            throw new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "repo missing");
        }
    }

    static class TestApplicationException extends ApplicationException {
        TestApplicationException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }
}
