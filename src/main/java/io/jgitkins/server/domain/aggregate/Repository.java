package io.jgitkins.server.domain.aggregate;

import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Repository Aggregate Root
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Repository implements AggregateRoot<RepositoryId> {

    private final RepositoryId id;
    private final OrganizeId organizeId;
    private final RepositoryName name;
    private final RepositoryPath path;
    private final BranchName defaultBranch;
    private final RepositoryVisibility visibility;
    private final String description;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String repositoryType;
    private final UserId ownerId;
    private final String credentialId;
    private final String clonePath;
    private final LocalDateTime lastSyncedAt;
    private final boolean requiresInitialContent;

    public static Repository create(Long organizeId,
                                    String repoName,
                                    String path,
                                    String branch,
                                    String visibility,
                                    String repositoryType,
                                    Long ownerId,
                                    String description,
                                    String clonePath,
                                    String credentialId,
                                    boolean initializeWithReadme,
                                    String initialCommitMessage) {

        validateCreateArgs(organizeId, repoName, initializeWithReadme, branch, initialCommitMessage);

        String normalizedBranch = normalizeBranch(branch, repoName);
        String normalizedPath = normalizePath(path, repoName);
        RepositoryVisibility resolvedVisibility = normalizeVisibility(visibility);
        String resolvedType = normalizeType(repositoryType);
        UserId owner = normalizeOwner(ownerId);

        return register(OrganizeId.of(organizeId),
                        RepositoryName.from(repoName),
                        RepositoryPath.from(normalizedPath),
                        BranchName.of(normalizedBranch),
                        resolvedVisibility,
                        resolvedType,
                        owner,
                        description,
                        clonePath,
                        credentialId,
                        initializeWithReadme);
    }

    /**
     * Repository 생성 팩토리
     */
    public static Repository register(OrganizeId organizeId,
                                      RepositoryName name,
                                      RepositoryPath path,
                                      BranchName defaultBranch,
                                      RepositoryVisibility visibility,
                                      String repositoryType,
                                      UserId ownerId,
                                      String description,
                                      String clonePath,
                                      String credentialId,
                                      boolean requiresInitialContent) {
        LocalDateTime now = LocalDateTime.now();
        return new Repository(
                null,
                organizeId,
                name,
                path,
                defaultBranch,
                visibility,
                description,
                now,
                now,
                repositoryType,
                ownerId,
                credentialId,
                clonePath,
                null,
                requiresInitialContent
        );
    }

    public Repository withIdentity(RepositoryId repositoryId,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        return new Repository(repositoryId,
                              organizeId,
                              name,
                              path,
                              defaultBranch,
                              visibility,
                              description,
                              createdAt,
                              updatedAt,
                              repositoryType,
                              ownerId,
                              credentialId,
                              clonePath,
                              lastSyncedAt,
                              requiresInitialContent);
    }

    public Repository markSynced(LocalDateTime syncedAt) {
        return new Repository(id,
                organizeId,
                name,
                path,
                defaultBranch,
                visibility,
                description,
                createdAt,
                updatedAt,
                repositoryType,
                ownerId,
                credentialId,
                clonePath,
                syncedAt,
                false
        );
    }

    public Repository updateMetadata(RepositoryName newName,
                                     RepositoryPath newPath,
                                     BranchName newDefaultBranch,
                                     RepositoryVisibility newVisibility,
                                     String newRepositoryType,
                                     UserId newOwner,
                                     String newDescription,
                                     String newClonePath,
                                     String newCredentialId) {
        return new Repository(id,
                              organizeId,
                              newName != null ? newName : name,
                              newPath != null ? newPath : path,
                              newDefaultBranch != null ? newDefaultBranch : defaultBranch,
                              newVisibility != null ? newVisibility : visibility,
                              newDescription != null ? newDescription : description,
                              createdAt,
                              updatedAt,
                              newRepositoryType != null ? newRepositoryType : repositoryType,
                              newOwner != null ? newOwner : ownerId,
                              newCredentialId != null ? newCredentialId : credentialId,
                              newClonePath != null ? newClonePath : clonePath,
                              lastSyncedAt,
                              requiresInitialContent);
    }

    public static Repository rehydrate(RepositoryId repositoryId,
                                       OrganizeId organizeId,
                                       RepositoryName name,
                                       RepositoryPath path,
                                       BranchName defaultBranch,
                                       RepositoryVisibility visibility,
                                       String repositoryType,
                                       UserId ownerId,
                                       String description,
                                       String clonePath,
                                       String credentialId,
                                       LocalDateTime createdAt,
                                       LocalDateTime updatedAt,
                                       LocalDateTime lastSyncedAt) {
        return new Repository(repositoryId,
                              organizeId,
                              name,
                              path,
                              defaultBranch,
                              visibility,
                              description,
                              createdAt,
                              updatedAt,
                              repositoryType,
                              ownerId,
                              credentialId,
                              clonePath,
                              lastSyncedAt,
                              lastSyncedAt == null);
    }

    private static void validateCreateArgs(Long organizeId,
                                           String repoName,
                                           boolean initializeWithReadme,
                                           String branch,
                                           String initialCommitMessage) {
        if (organizeId == null) {
            throw new IllegalArgumentException("organizeId must be provided");
        }
        if (repoName == null || repoName.isBlank()) {
            throw new IllegalArgumentException("repoName must not be blank");
        }
        if (branch == null || branch.isBlank()) {
            throw new IllegalArgumentException("mainBranch must be provided");
        }

        if (!initializeWithReadme) {
            return;
        }
        if (initialCommitMessage == null || initialCommitMessage.isBlank()) {
            throw new IllegalArgumentException("message is required when initializing with README");
        }
    }

    private static String normalizeBranch(String branch, String repoNameFallback) {
        return (branch == null || branch.isBlank()) ? defaultBranchName(repoNameFallback) : branch.trim();
    }

    private static String normalizePath(String path, String repoNameFallback) {
        return (path == null || path.isBlank()) ? repoNameFallback : path.trim();
    }

    private static RepositoryVisibility normalizeVisibility(String visibility) {
        return visibility != null ? RepositoryVisibility.from(visibility) : RepositoryVisibility.PRIVATE;
    }

    private static String normalizeType(String repositoryType) {
        return repositoryType != null ? repositoryType.trim().toUpperCase() : "GIT";
    }

    private static UserId normalizeOwner(Long ownerId) {
        return ownerId != null ? UserId.of(ownerId) : null;
    }

    private static String defaultBranchName(String repoNameFallback) {
        return "main";
    }
}
