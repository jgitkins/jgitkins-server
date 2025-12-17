package io.jgitkins.server.domain.exception;

public class RunnerTokenMissingException extends DomainException {

    public RunnerTokenMissingException() {
        super("Runner activation token is required");
    }
}
