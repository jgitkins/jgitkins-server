package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.RepositoryResult;
import io.jgitkins.server.application.dto.UpdateRepositoryCommand;
import io.jgitkins.server.application.port.in.CreateRepositoryUseCase;
import io.jgitkins.server.application.port.in.DeleteRepositoryUseCase;
import io.jgitkins.server.application.port.in.LoadAllFilesUseCase;
import io.jgitkins.server.application.port.in.LoadBranchCommitHistoriesUseCase;
import io.jgitkins.server.application.port.in.LoadCommitDetailUseCase;
import io.jgitkins.server.application.port.in.LoadRepositoryUseCase;
import io.jgitkins.server.application.port.in.LoadTreeUseCase;
import io.jgitkins.server.application.port.in.UpdateRepositoryUseCase;
import io.jgitkins.server.application.port.in.UploadFileUseCase;
import io.jgitkins.server.application.port.out.CreateRepositoryPort;
import io.jgitkins.server.application.port.out.DeleteRepositoryPort;
import io.jgitkins.server.application.port.out.UpdateHeadReferencePort;
import io.jgitkins.server.application.port.out.LoadAllFilesPort;
import io.jgitkins.server.application.port.out.LoadBranchCommitHistoriesPort;
import io.jgitkins.server.application.port.out.LoadCommitDetailPort;
import io.jgitkins.server.application.port.out.LoadTreePort;
import io.jgitkins.server.application.port.out.RepositoryCommitPort;
import io.jgitkins.server.application.port.out.RepositoryContentPort;
import io.jgitkins.server.application.port.out.RepositoryPersistencePort;
import io.jgitkins.server.application.port.out.OrganizePersistencePort;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import io.jgitkins.server.domain.model.vo.RepositoryPath;
import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService implements CreateRepositoryUseCase,
                                          LoadRepositoryUseCase,
                                          UpdateRepositoryUseCase,
                                          DeleteRepositoryUseCase,
                                          UploadFileUseCase,
                                          LoadTreeUseCase,
                                          LoadAllFilesUseCase,
                                          LoadCommitDetailUseCase,
                                          LoadBranchCommitHistoriesUseCase {

    private final CreateRepositoryPort createRepositoryPort;
    private final LoadTreePort getTreePort;
    private final LoadAllFilesPort getAllFilesPort;
    private final LoadCommitDetailPort getCommitDetailPort;
    private final LoadBranchCommitHistoriesPort getBranchCommitHistoriesPort;

    private final UpdateHeadReferencePort updateHeadReferencePort;
    private final DeleteRepositoryPort deleteRepositoryPort;
    private final RepositoryCommitPort repositoryCommitPort;
    private final RepositoryContentPort repositoryContentPort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final OrganizePersistencePort organizePersistencePort;
    private final RepositoryApplicationMapper repositoryApplicationMapper;

    @Override
    @Transactional
    public RepositoryResult create(CreateRepositoryCommand command) {

        OrganizeId organizeId = OrganizeId.of(command.getOrganizeId());
        RepositoryName repositoryName = RepositoryName.from(command.getRepoName());
        ensureRepositoryNameUnique(organizeId, repositoryName, null);
        Organize organize = loadOrganize(organizeId);
        String taskCd = organize.getName().getValue();
//        String organizeSlug = organize.getPath().getValue();
        RepositoryPath repositoryPath = resolveRepositoryPath(command.getPath(), command.getRepoName());
        String clonePath = buildClonePath(taskCd, repositoryPath.getValue());

        Repository repository = Repository.create(command.getOrganizeId(),
                                                  command.getRepoName(),
                                                  repositoryPath.getValue(),
                                                  command.getMainBranch(),
                                                  command.getVisibility(),
                                                  command.getRepositoryType(),
                                                  command.getOwnerId(),
                                                  command.getDescription(),
                                                  clonePath,
                                                  command.getCredentialId(),
                                                  command.isReadme(),
                                                  command.getMessage());
        
        Repository savedRepository = repositoryPersistencePort.save(repository);
        createRepositoryPort.create(taskCd, command.getRepoName());

        if (!savedRepository.isRequiresInitialContent()) {
            updateHeadReferencePort.updateHeadReference(taskCd, command.getRepoName(), command.getMainBranch());
            Repository initialized = repositoryPersistencePort.update(savedRepository.markSynced(LocalDateTime.now()));
            return repositoryApplicationMapper.toDto(initialized);
        }

        List<CommitFile> files = repositoryContentPort.prepareInitialFiles(command.getRepoName());
        repositoryCommitPort.commit(taskCd,
                                    command.getRepoName(),
                                    command.getMainBranch(),
                                    command.getMessage(),
                                    command.getAuthorName(),
                                    command.getAuthorEmail(),
                                    files);
        updateHeadReferencePort.updateHeadReference(taskCd, command.getRepoName(), command.getMainBranch());

        Repository initialized = repositoryPersistencePort.update(savedRepository.markSynced(LocalDateTime.now()));
        return repositoryApplicationMapper.toDto(initialized);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryResult getRepository(Long repositoryId) {
        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found: " + repositoryId));
        return repositoryApplicationMapper.toDto(repository);
    }

    @Override
    @Transactional
    public RepositoryResult updateRepository(Long repositoryId, UpdateRepositoryCommand command) {
        Repository repository = repositoryPersistencePort.findById(RepositoryId.of(repositoryId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found: " + repositoryId));

        String normalizedType = command.getRepositoryType() != null
                ? command.getRepositoryType().trim().toUpperCase()
                : null;

        if (command.getName() != null) {
            RepositoryName newName = RepositoryName.from(command.getName());
            if (!repository.getName().getValue().equalsIgnoreCase(newName.getValue())) {
                ensureRepositoryNameUnique(repository.getOrganizeId(), newName, repository.getId());
            }
        }

        RepositoryPath newPath = command.getPath() != null ? RepositoryPath.from(command.getPath()) : null;
        String organizeSlug = loadOrganize(repository.getOrganizeId()).getPath().getValue();
        String effectiveRepoPath = newPath != null ? newPath.getValue() : repository.getPath().getValue();
        String clonePath = buildClonePath(organizeSlug, effectiveRepoPath);

        Repository updated = repository.updateMetadata(
                command.getName() != null ? RepositoryName.from(command.getName()) : null,
                newPath,
                command.getDefaultBranch() != null ? BranchName.of(command.getDefaultBranch()) : null,
                command.getVisibility() != null ? RepositoryVisibility.from(command.getVisibility()) : null,
                normalizedType,
                command.getOwnerId() != null ? UserId.of(command.getOwnerId()) : null,
                command.getDescription(),
                clonePath,
                command.getCredentialId()
        );

        if (command.getLastSyncedAt() != null) {
            updated = updated.markSynced(command.getLastSyncedAt());
        }

        Repository persisted = repositoryPersistencePort.update(updated);
        return repositoryApplicationMapper.toDto(persisted);
    }

    @Override
    @Transactional
    public void deleteRepository(Long repositoryId) {
        RepositoryId id = RepositoryId.of(repositoryId);
        Repository repository = repositoryPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND, "Repository not found: " + repositoryId));
        deleteRepositoryDirectory(repository);
        repositoryPersistencePort.delete(id);
    }

    @Override
    public void uploadFileToRepository(String taskCd, String repoName, String branch, MultipartFile file, FileUploadInfo request) throws IOException {
        List<CommitFile> files = repositoryContentPort.prepareUploadFiles(file, request);
        repositoryCommitPort.commit(taskCd,
                                    repoName,
                                    branch,
                                    request.getCommitMessage(),
                                    request.getAuthorName(),
                                    request.getAuthorEmail(),
                                    files);
    }

    @Override
    public List<FileEntry> getTree(String taskCd, String repoName, String branch, String directory) throws IOException {
        return getTreePort.getTree(taskCd, repoName, branch, directory);
    }

    @Override
    public List<FileEntry> getAllFiles(String taskCd, String repoName, String reference) {
        return getAllFilesPort.getAllFiles(taskCd, repoName, reference);
    }

    @Override
    public CommitHistory getCommitDetail(String taskCd, String repoName, String commitHash) throws IOException {
        return getCommitDetailPort.getCommitDetail(taskCd, repoName, commitHash);
    }

    @Override
    public List<CommitHistory> getBranchCommitHistories(String taskCd, String repoName, String branch) throws IOException {
        return getBranchCommitHistoriesPort.getCommitHistories(taskCd, repoName, branch);
    }

    private void ensureRepositoryNameUnique(OrganizeId organizeId,
                                            RepositoryName name,
                                            RepositoryId currentRepositoryId) {
        repositoryPersistencePort.findByOrganizeAndName(organizeId, name)
                .ifPresent(existing -> {
                    if (currentRepositoryId == null || !existing.getId().equals(currentRepositoryId)) {
                        throw new ConflictException(ErrorCode.REPOSITORY_ALREADY_EXISTS,
                                "Repository name already exists in organize: " + name.getValue());
                    }
                });
    }

    private void deleteRepositoryDirectory(Repository repository) {
        Organize organize = loadOrganize(repository.getOrganizeId());
        deleteRepositoryPort.delete(organize.getName().getValue(), repository.getName().getValue());
    }

    private Organize loadOrganize(OrganizeId organizeId) {
        return organizePersistencePort.findById(organizeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId.getValue()));
    }

    private RepositoryPath resolveRepositoryPath(String requestedPath, String repoName) {
        String candidate = (requestedPath == null || requestedPath.isBlank()) ? repoName : requestedPath;
        return RepositoryPath.from(candidate);
    }

    private String buildClonePath(String organizeSlug, String repoPath) {
        String orgSegment = trimSlashes(organizeSlug);
        String repoSegment = trimSlashes(repoPath);
        if (!repoSegment.endsWith(".git")) {
            repoSegment = repoSegment + ".git";
        }
        return "/" + orgSegment + "/" + repoSegment;
    }

    private String trimSlashes(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
