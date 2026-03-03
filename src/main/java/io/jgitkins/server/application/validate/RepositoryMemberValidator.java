package io.jgitkins.server.application.validate;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.command.RepositoryMemberAddCommand;
import io.jgitkins.server.application.port.out.RepositoryMemberPort;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryMemberValidator {

    private final RepositoryMemberPort repositoryMemberPort;

    public void validateAddCommand(RepositoryMemberAddCommand command) {
        if (command == null || command.getRepositoryId() == null || command.getUserId() == null) {
            throw new JgitkinsException(ApplicationErrorCode.BAD_REQUEST, "RepositoryId and UserId are required to add a repository member");
        }
    }

    public void validateRepositoryId(Long repositoryId) {
        if (repositoryId == null) {
            throw new JgitkinsException(ApplicationErrorCode.BAD_REQUEST, "RepositoryId is required");
        }
    }

    public void validateMemberIdentifiers(Long repositoryId, Long userId) {
        if (repositoryId == null || userId == null) {
            throw new JgitkinsException(ApplicationErrorCode.BAD_REQUEST, "RepositoryId and UserId are required");
        }
    }

    public boolean isAlreadyMember(RepositoryId repositoryId, UserId userId) {
        return repositoryMemberPort.existsByRepositoryAndUser(repositoryId, userId);
    }
}
