package io.jgitkins.server.application.support;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryLookupService {

    private final RepositoryPort repositoryPort;
    private final UserPort userPort;
    private final OrganizePort organizePort;
    private final OrganizeMemberPort organizeMemberPort;

    public Optional<Repository> findByPath(String namespace, String repoName) {
        if (namespace == null || namespace.isBlank() || repoName == null || repoName.isBlank()) {
            return Optional.empty();
        }

        String normalizedNamespace = namespace.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        String normalizedRepo = repoName.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        
        // 1. Clone Path 기반 조회 (Fastest)
        String clonePath = RepositoryPathHelper.buildClonePath(normalizedNamespace, normalizedRepo);
        Optional<Repository> byClonePath = repositoryPort.findByClonePath(clonePath);
        if (byClonePath.isPresent()) {
            return byClonePath;
        }

        // 2. 사용자/조직 네임스페이스 기반 조회
        Optional<Repository> userRepository = userPort.findByUsername(normalizedNamespace)
                .flatMap(user -> repositoryPort.findByOwnerAndName(
                        OwnerType.USER,
                        OwnerId.of(user.getId()),
                        RepositoryName.from(normalizedRepo)
                ));

        Optional<Repository> organizeRepository = findOrganizeByNamespace(normalizedNamespace)
                .flatMap(organize -> repositoryPort.findByOwnerAndPath(
                        OwnerType.ORGANIZATION,
                        OwnerId.of(organize.getId().getValue()),
                        RepositoryPath.from(normalizedRepo)
                ));

        if (userRepository.isPresent() && organizeRepository.isPresent()) {
            log.warn("Ambiguous repository path. namespace={}, repoName={}. Prefer USER-owned repository.", namespace, repoName);
            return userRepository;
        }
        return userRepository.isPresent() ? userRepository : organizeRepository;
    }

    public boolean isVisibleToRequester(Repository repository,
                                         Optional<Long> requesterId,
                                         Map<OrganizeId, Boolean> membershipCache) {
        if (repository == null) return false;
        if (repository.getVisibility() == RepositoryVisibility.PUBLIC) return true;
        if (requesterId.isEmpty()) return false;

        Long userId = requesterId.get();
        if (repository.getOwnerType() == OwnerType.USER) {
            return repository.getOwnerId() != null && userId.equals(repository.getOwnerId().getValue());
        }
        if (repository.getOwnerType() == OwnerType.ORGANIZATION && repository.getOwnerId() != null) {
            OrganizeId organizeId = OrganizeId.of(repository.getOwnerId().getValue());
            return membershipCache.computeIfAbsent(organizeId,
                    id -> organizeMemberPort.existsByOrganizeAndUser(id, UserId.of(userId)));
        }
        return false;
    }

    public boolean isVisibleToUserOwner(Repository repository,
                                         Optional<Long> requesterId,
                                         Long ownerId) {
        if (repository == null) return false;
        if (repository.getVisibility() == RepositoryVisibility.PUBLIC) return true;
        return requesterId.isPresent() && ownerId != null && ownerId.equals(requesterId.get());
    }

    private Optional<io.jgitkins.server.domain.aggregate.Organize> findOrganizeByNamespace(String namespace) {
        try {
            return organizePort.findByName(OrganizeName.from(namespace));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
