package io.jgitkins.server.domain.exception;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.error.DomainErrorCode;

public class UsernameAlreadySetException extends JgitkinsException {

    public UsernameAlreadySetException() {
        super(DomainErrorCode.USERNAME_ALREADY_SET, "Username already set");
    }
}
