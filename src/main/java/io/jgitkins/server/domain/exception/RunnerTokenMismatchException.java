package io.jgitkins.server.domain.exception;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;

public class RunnerTokenMismatchException extends JgitkinsException {

    public RunnerTokenMismatchException() {
        super(DomainErrorCode.RUNNER_TOKEN_INVALID, "Runner token does not match activation request");
    }
}
