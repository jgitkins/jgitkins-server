package io.jgitkins.server.application.event;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.port.out.*;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.event.RepositoryProvisionedEvent;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryProvisionedEventListener {

    private final OrganizePersistencePort organizePersistencePort;
    private final RepositoryContentPort repositoryContentPort;
    private final RepositoryCommitPort repositoryCommitPort;
    private final UpdateHeadReferencePort updateHeadReferencePort;
    private final RepositoryPersistencePort repositoryPersistencePort;
    private final BranchPersistenceCommandPort branchPersistenceCommandPort;


    // post progressing
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // contain require_new
    @Transactional(propagation = REQUIRES_NEW)
    public void onRepositoryProvisioned(RepositoryProvisionedEvent event) {
        RepositoryName repositoryName = event.getName();
        String repoNameValue = repositoryName.getValue();
        String branchName = event.getDefaultBranch().getValue();
        String organizeSlug = loadOrganizeSlug(event);

        Repository repository = repositoryPersistencePort.findByOrganizeAndName(event.getOrganizeId(), repositoryName)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REPOSITORY_NOT_FOUND,
                        "Repository not found for event: " + repoNameValue));

        // 기본 브랜치 엔트리를 미리 생성해 애플리케이션 상태를 일관되게 유지한다.
        Branch defaultBranch = Branch.create(repository.getId().getValue(),
                                             branchName,
                                             false,
                                             true);
        branchPersistenceCommandPort.create(defaultBranch);

        if (event.getInitialCommitOptions() != null && event.getInitialCommitOptions().requiresInitialContent()) {
            List<CommitFile> files = repositoryContentPort.prepareInitialFiles(repoNameValue);
            repositoryCommitPort.commit(organizeSlug,
                                        repoNameValue,
                                        branchName,
                                        event.getInitialCommitOptions().commitMessage(),
                                        event.getInitialCommitOptions().authorName(),
                                        event.getInitialCommitOptions().authorEmail(),
                                        files);
            updateHeadReferencePort.updateHeadReference(organizeSlug, repoNameValue, branchName);
            log.info("repository has initialized with readme");

            repositoryPersistencePort.update(repository.markInit(LocalDateTime.now()));
        }
    }

    private String loadOrganizeSlug(RepositoryProvisionedEvent event) {
        return organizePersistencePort.findById(event.getOrganizeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + event.getOrganizeId().getValue()))
                .getName()
                .getValue();
    }
}
