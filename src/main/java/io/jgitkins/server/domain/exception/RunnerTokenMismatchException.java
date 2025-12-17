package io.jgitkins.server.domain.exception;

public class RunnerTokenMismatchException extends DomainException {

    public RunnerTokenMismatchException() {
        super("Runner token does not match activation request");
    }
}
