package io.jgitkins.server.domain.exception;

/**
 * Base type for domain-specific rule violations. Keeping this separate from
 * application exceptions allows services/controllers to translate domain
 * failures into transport-friendly responses.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
