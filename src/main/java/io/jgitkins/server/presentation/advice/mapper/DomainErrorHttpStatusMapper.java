package io.jgitkins.server.presentation.advice.mapper;

import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.domain.error.DomainErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DomainErrorHttpStatusMapper implements ErrorHttpStatusMapper {

    @Override
    public boolean supports(ErrorCode errorCode) {
        return errorCode instanceof DomainErrorCode;
    }

    @Override
    public HttpStatus map(ErrorCode errorCode) {
        DomainErrorCode domainErrorCode = (DomainErrorCode) errorCode;
        return switch (domainErrorCode) {
            case DOM_RUNNER_ALREADY_ACTIVE -> HttpStatus.CONFLICT;
            case DOM_RUNNER_TOKEN_INVALID,
                 DOM_USERNAME_ALREADY_SET -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DOM_RULE_VIOLATION -> HttpStatus.BAD_REQUEST;
        };
    }
}

