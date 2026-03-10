package io.jgitkins.server.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.in.SignupUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.validate.ActivationValidator;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.domain.exception.UserAlreadyActivatedException;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.vo.Username;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService implements SignupUseCase {

    private final CurrentUserPort currentUserPort;
    private final UserPort userPort;
    private final ActivationValidator activationValidator;

    @Override
    @Transactional
    public void activate(String username) {
        Username requested = activationValidator.validateUsername(username);
        Long userId = currentUserId();
        User user = loadUser(userId);

        activationValidator.validateUsernameNotTaken(requested, userId);
        activationValidator.validateOrganizeNameNotTakenIfCompatible(requested);
        activationValidator.validateUserHasNoRepositories(userId);

        User updated = activateUser(user, requested);
        userPort.save(updated);
    }

    private Long currentUserId() {
        return currentUserPort.currentUserId()
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.presentation.common.error.PresentationErrorCode.UNAUTHORIZED, "Unauthenticated"));
    }

    private User loadUser(Long userId) {
        return userPort.findById(userId)
                .orElseThrow(() -> new JgitkinsException(io.jgitkins.server.application.common.error.ApplicationErrorCode.USER_NOT_FOUND, "User not found"));
    }

    private User activateUser(User user, Username requested) {
        try {
            return user.activateWithUsername(requested);
        } catch (UserAlreadyActivatedException ex) {
            throw new JgitkinsException(DomainErrorCode.USER_ALREADY_ACTIVATED, ex.getMessage(), ex);
        }
    }
}
