package io.jgitkins.server.presentation.advice;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.presentation.common.error.PresentationErrorCode;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;

public final class LegacyErrorCodeBridge {

    private LegacyErrorCodeBridge() {
    }

    public static ErrorCode from(io.jgitkins.server.common.error.ErrorCode errorCode) {
        if (errorCode instanceof ErrorCode legacy) {
            return legacy;
        }
        if (errorCode instanceof PresentationErrorCode) {
            return ErrorCode.BAD_REQUEST;
        }
        if (errorCode instanceof ApplicationErrorCode applicationErrorCode) {
            return mapApplication(applicationErrorCode);
        }
        if (errorCode instanceof DomainErrorCode domainErrorCode) {
            return mapDomain(domainErrorCode);
        }
        if (errorCode instanceof InfrastructureErrorCode) {
            return ErrorCode.INTERNAL_SERVER_ERROR;
        }
        return ErrorCode.INTERNAL_SERVER_ERROR;
    }

    private static ErrorCode mapApplication(ApplicationErrorCode code) {
        return switch (code) {
            case APP_BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            case APP_UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case APP_FORBIDDEN -> ErrorCode.FORBIDDEN;
            case APP_NOT_FOUND -> ErrorCode.REPOSITORY_NOT_FOUND;
            case APP_CONFLICT -> ErrorCode.REPOSITORY_ALREADY_EXISTS;
            case APP_UNPROCESSABLE -> ErrorCode.REPOSITORY_DOES_NOT_INITIALIZED;
            case APP_INTERNAL_SERVER_ERROR -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }

    private static ErrorCode mapDomain(DomainErrorCode code) {
        return switch (code) {
            case DOM_RUNNER_ALREADY_ACTIVE -> ErrorCode.RUNNER_ALREADY_ACTIVE;
            case DOM_RUNNER_TOKEN_INVALID -> ErrorCode.RUNNER_INVALID_TOKEN;
            case DOM_USERNAME_ALREADY_SET -> ErrorCode.USERNAME_ALREADY_EXISTS;
            case DOM_RULE_VIOLATION -> ErrorCode.BAD_REQUEST;
        };
    }
}

