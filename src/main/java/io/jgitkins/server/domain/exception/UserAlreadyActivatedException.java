package io.jgitkins.server.domain.exception;

public class UserAlreadyActivatedException extends RuntimeException {

    public UserAlreadyActivatedException() {
        super("User is already activated");
    }
}
