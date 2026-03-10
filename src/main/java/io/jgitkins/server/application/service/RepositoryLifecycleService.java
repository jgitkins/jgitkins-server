package io.jgitkins.server.application.service;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.common.event.DomainEventPublisher;
import io.jgitkins.server.application.dto.command.RepositoryCreateCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.in.RepositoryCreateUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeleteUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.RepositoryGitPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.support.RepositoryLookupService;
import io.jgitkins.server.application.support.RepositoryNamespaceResolver;
import io.jgitkins.server.application.validate.RepositoryValidator;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.*;
import io.jgitkins.server.infrastructure.support.RepositoryPathHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryLifecycleService implements RepositoryCreateUseCase,
                RepositoryLoadUseCase,
                RepositoryDeleteUseCase {

        private final RepositoryNamespaceResolver repositoryNamespaceResolver;
        private final RepositoryApplicationMapper repositoryApplicationMapper;
        private final DomainEventPublisher domainEventPublisher;

        private final RepositoryGitPort repositoryGitPort;
        private final RepositoryPort repositoryPort;
        private final CurrentUserPort currentUserPort;
        private final UserPort userPort;

        private final RepositoryValidator repositoryValidator;
        private final RepositoryLookupService repositoryLookupService;

        @Override
        @Transactional
        public RepositoryResult create(RepositoryCreateCommand command) {
                long startedAt = System.nanoTime();

                // 1. 입력 검증 및 VO 생성 (Fast-Fail)
                RepositoryName repositoryName = RepositoryName.from(command.getRepoName());
                RepositoryPath repositoryPath = RepositoryPath.from(command.getRepoName());
                OwnerType ownerType = OwnerType.from(command.getOwnerType());
                BranchName defaultBranch = BranchName.of(command.getMainBranch());
                RepositoryVisibility visibility = command.getVisibility() != null
                                ? RepositoryVisibility.from(command.getVisibility())
                                : RepositoryVisibility.PRIVATE;

                InitialCommitOptions initialCommitOptions = InitialCommitOptions.of(
                                command.isReadme(), command.getMessage(), command.getAuthorName(),
                                command.getAuthorEmail());

                // 2. 비즈니스 규칙 검증 (Validator 위임)
                repositoryValidator.validateCreation(ownerType, command.getOrganizeId(), repositoryName);

                // 3. 컨텍스트 준비
                OwnerId ownerId = resolveOwnerId(ownerType, command.getOrganizeId());
                String namespace = repositoryNamespaceResolver.resolve(ownerType, ownerId);
                String clonePath = RepositoryPathHelper.buildClonePath(namespace, repositoryPath.getValue());

                // 4. 애그리게이트 생성 및 저장
                Repository repository = Repository.create(ownerType, ownerId, repositoryName, repositoryPath,
                                defaultBranch, visibility, command.getDescription(), clonePath,
                                command.getCredentialId(), initialCommitOptions);

                Repository saved = repositoryPort.save(repository);
                repositoryGitPort.create(namespace, repositoryName.getValue());

                publishDomainEvents(saved);

                long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
                log.info("Repository created. id={}, owner={}, name={}, duration={}ms",
                                saved.getId().getValue(), ownerId, repositoryName.getValue(), durationMs);

                return repositoryApplicationMapper.toDto(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public RepositoryResult getRepository(Long repositoryId) {
                Repository repository = repositoryPort.findById(RepositoryId.of(repositoryId))
                                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                                                "Repository not found: " + repositoryId));
                return repositoryApplicationMapper.toDto(repository);
        }

        @Override
        @Transactional(readOnly = true)
        public RepositoryResult getRepositoryByPath(String namespace, String repoName) {
                Repository repository = repositoryLookupService.findByPath(namespace, repoName)
                                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                                                String.format("Repository not found: %s/%s", namespace, repoName)));
                return repositoryApplicationMapper.toDto(repository);
        }

        @Override
        @Transactional(readOnly = true)
        public List<RepositoryResult> getRepositories() {
                Optional<Long> requesterId = currentUserPort.currentUserId();
                Map<OrganizeId, Boolean> membershipCache = new HashMap<>();

                return repositoryPort.findAll().stream()
                                .filter(repo -> repositoryLookupService.isVisibleToRequester(repo, requesterId,
                                                membershipCache))
                                .map(repositoryApplicationMapper::toDto)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<RepositoryResult> getRepositoriesByUsername(String username) {
                // TODO: Presentation 계층으로 이관 (Validator 통해 처리하기)
                String normalizedUsername = username != null ? username.trim() : "";

                Long ownerId = userPort.findUserIdByUsername(normalizedUsername)
                                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.USER_NOT_FOUND,
                                                "User not found: " + normalizedUsername));

                Optional<Long> requesterId = currentUserPort.currentUserId();
                return repositoryPort.findAllByOwner(OwnerType.USER, OwnerId.of(ownerId)).stream()
                                .filter(repo -> repositoryLookupService.isVisibleToUserOwner(repo, requesterId,
                                                ownerId))
                                .map(repositoryApplicationMapper::toDto)
                                .toList();
        }

        @Override
        @Transactional
        public void deleteRepository(Long repositoryId) {
                RepositoryId id = RepositoryId.of(repositoryId);
                Repository repository = repositoryPort.findById(id)
                                .orElseThrow(() -> new JgitkinsException(ApplicationErrorCode.REPOSITORY_NOT_FOUND,
                                                "Repository not found: " + repositoryId));

                repositoryValidator.enforceDeletionPermission(repository);

                String namespace = repositoryNamespaceResolver.resolve(repository);
                repositoryGitPort.delete(namespace, repository.getName().getValue());

                repositoryPort.delete(id);
        }

        private OwnerId resolveOwnerId(OwnerType ownerType, Long organizeId) {
                return ownerType == OwnerType.ORGANIZATION ? OwnerId.of(organizeId)
                                : OwnerId.of(repositoryValidator.requireCurrentUserId());
        }

        private void publishDomainEvents(Repository repository) {
                domainEventPublisher.publish(repository.getDomainEvents());
                repository.clearDomainEvents();
        }
}
