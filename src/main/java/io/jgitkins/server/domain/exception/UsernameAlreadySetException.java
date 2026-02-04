package io.jgitkins.server.domain.exception;

public class UsernameAlreadySetException extends DomainException {

    public UsernameAlreadySetException() {
        super("Username already set");
    }
}
