package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.command.RepositoryMemberAddCommand;
import io.jgitkins.server.application.dto.result.RepositoryMemberSummary;
import io.jgitkins.server.application.port.in.RepositoryMemberAddUseCase;
import io.jgitkins.server.application.port.in.RepositoryMemberQueryUseCase;
import io.jgitkins.server.application.port.in.RepositoryMemberRemoveUseCase;
import io.jgitkins.server.application.port.out.RepositoryMemberPersistencePort;
import io.jgitkins.server.domain.model.RepositoryMember;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryMemberRole;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepositoryMemberService implements RepositoryMemberAddUseCase,
                                                RepositoryMemberRemoveUseCase,
                                                RepositoryMemberQueryUseCase {

    private final RepositoryMemberPersistencePort repositoryMemberPersistencePort;

    @Override
    @Transactional
    public void addRepositoryMember(RepositoryMemberAddCommand command) {
        validateCommand(command);
        RepositoryId repositoryId = RepositoryId.of(command.getRepositoryId());
        UserId userId = UserId.of(command.getUserId());
        if (repositoryMemberPersistencePort.existsByRepositoryAndUser(repositoryId, userId)) {
            return;
        }
        RepositoryMemberRole role = command.getRole() != null ? command.getRole() : RepositoryMemberRole.READER;
        RepositoryMember member = RepositoryMember.create(repositoryId, userId, role, null);
        repositoryMemberPersistencePort.save(member);
    }

    @Override
    @Transactional
    public void removeRepositoryMember(Long repositoryId, Long userId) {
        if (repositoryId == null || userId == null) {
            throw new IllegalArgumentException("RepositoryId and UserId are required to remove a repository member");
        }
        repositoryMemberPersistencePort.deleteByRepositoryAndUser(RepositoryId.of(repositoryId), UserId.of(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<RepositoryMemberSummary> getRepositoryMembers(Long repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("RepositoryId is required to load repository members");
        }
        return repositoryMemberPersistencePort.findAllByRepository(RepositoryId.of(repositoryId))
                .stream()
                .map(member -> new RepositoryMemberSummary(
                        member.getUserId().getValue(),
                        member.getRole(),
                        member.getAddedAt()
                ))
                .toList();
    }

    private void validateCommand(RepositoryMemberAddCommand command) {
        if (command == null || command.getRepositoryId() == null || command.getUserId() == null) {
            throw new IllegalArgumentException("RepositoryId and UserId are required to add a repository member");
        }
    }
}
