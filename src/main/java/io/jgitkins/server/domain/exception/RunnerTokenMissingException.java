package io.jgitkins.server.domain.exception;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;

public class RunnerTokenMissingException extends JgitkinsException {

    public RunnerTokenMissingException() {
        super(DomainErrorCode.DOM_RUNNER_TOKEN_INVALID, "Runner activation token is required");
    }
}
