package io.jgitkins.server.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.in.UserProfileUpdateUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.validate.UserProfileValidator;
import io.jgitkins.server.domain.error.DomainErrorCode;
import io.jgitkins.server.domain.exception.UsernameAlreadySetException;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.vo.Username;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUpdateUseCase {

    private final CurrentUserPort currentUserPort;
    private final UserPort userPort;
    private final UserProfileValidator userProfileValidator;

    @Override
    @Transactional
    public void updateUsername(String username) {
        Username requested = userProfileValidator.validateUsername(username);
        Long userId = currentUserId();
        User user = loadUser(userId);

        userProfileValidator.validateUsernameNotTaken(requested, userId);
        userProfileValidator.validateOrganizeNameNotTakenIfCompatible(requested);
        userProfileValidator.validateUserHasNoRepositories(userId);

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
        } catch (UsernameAlreadySetException ex) {
            throw new JgitkinsException(DomainErrorCode.USERNAME_ALREADY_SET, ex.getMessage(), ex);
        }
    }
}
