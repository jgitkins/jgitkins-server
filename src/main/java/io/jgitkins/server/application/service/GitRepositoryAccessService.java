package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryMemberPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GitRepositoryAccessService {

    private final OrganizePort organizePort;
    private final RepositoryPort repositoryPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final RepositoryMemberPort repositoryMemberPort;

    public boolean canRead(String organizeSlug, String repositoryPath, Long userId) {
        return canAccess(organizeSlug, repositoryPath, userId);
    }

    public boolean canWrite(String organizeSlug, String repositoryPath, Long userId) {
        return canAccess(organizeSlug, repositoryPath, userId);
    }

    private boolean canAccess(String organizeSlug, String repositoryPath, Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<Repository> repository = resolveRepository(organizeSlug, repositoryPath);
        if (repository.isEmpty()) {
            return false;
        }
        Repository repo = repository.get();
        UserId uid = UserId.of(userId);
        if (repo.getOwnerId() != null && repo.getOwnerId().equals(uid)) {
            return true;
        }
        if (repositoryMemberPort.existsByRepositoryAndUser(repo.getId(), uid)) {
            return true;
        }
        return organizeMemberPort.existsByOrganizeAndUser(repo.getOrganizeId(), uid);
    }

    private Optional<Repository> resolveRepository(String organizeSlug, String repositoryPath) {
        if (organizeSlug == null || organizeSlug.isBlank() || repositoryPath == null || repositoryPath.isBlank()) {
            return Optional.empty();
        }
        Optional<Organize> organize = organizePort.findByName(OrganizeName.from(organizeSlug));
        if (organize.isEmpty()) {
            return Optional.empty();
        }
        OrganizeId organizeId = organize.get().getId();
        return repositoryPort.findByOrganizeAndPath(organizeId, RepositoryPath.from(repositoryPath));
    }
}
