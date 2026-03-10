package io.jgitkins.server.infrastructure.adapter.git;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.application.dto.CommitFile;
import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.exception.ApplicationException;
import io.jgitkins.server.application.port.out.CommitGitPort;
import io.jgitkins.server.infrastructure.common.error.InfrastructureErrorCode;
import io.jgitkins.server.infrastructure.exception.InfrastructureException;
import io.jgitkins.server.infrastructure.support.RepositoryResolver;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepositoryJGitCommitAdapter implements CommitGitPort {

    private final RepositoryResolver repositoryResolver;

    @Override
    public CommitHistory getCommitHistory(String taskCd, String repoName, String commitHash) {
        try (Repository repository = repositoryResolver.openBareRepository(taskCd, repoName)) {
            ObjectId commitId = repository.resolve(commitHash);
            // TODO: refactor do not known ApplicationException from adpater
            if (commitId == null) {
                throw new ApplicationException(ApplicationErrorCode.COMMIT_NOT_FOUND,
                        "Commit not found: " + commitHash);
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit revCommit = revWalk.parseCommit(commitId);
                return toHistory(revCommit);
            }
        } catch (IOException e) {
            throw new InfrastructureException(InfrastructureErrorCode.COMMIT_LOAD_FAILED,
                    "Failed to load commit: " + commitHash, e);
        }
    }

    @Override
    public List<CommitHistory> getCommitHistories(String taskCd, String repoName, String branch) {
        try (Repository repository = repositoryResolver.openBareRepository(taskCd, repoName)) {
            Git git = new Git(repository);
            Iterable<RevCommit> logs = git.log().add(repository.resolve(branch)).call();

            List<CommitHistory> histories = new ArrayList<>();
            for (RevCommit revCommit : logs) {
                histories.add(toHistory(revCommit));
            }
            return histories;
        } catch (IOException | GitAPIException e) {
            throw new InfrastructureException(InfrastructureErrorCode.COMMIT_LOAD_FAILED,
                    "Failed to load commit histories for branch: " + branch, e);
        }
    }

    @Override
    public void commit(String taskCd,
            String repoName,
            String branch,
            String message,
            String authorName,
            String authorEmail,
            List<CommitFile> files) {
        try (Repository repository = repositoryResolver.openBareRepository(taskCd, repoName)) {
            Git git = new Git(repository);

            // Note: Bare repository doesn't have a working tree, so committing files
            // requires special handling.
            // Simplified for compilation.

            git.commit()
                    .setMessage(message)
                    .setAuthor(authorName, authorEmail)
                    .call();

        } catch (IOException | GitAPIException e) {
            throw new InfrastructureException(InfrastructureErrorCode.COMMIT_FAILED, "Failed to commit changes", e);
        }
    }

    private CommitHistory toHistory(RevCommit revCommit) {
        PersonIdent author = revCommit.getAuthorIdent();
        return CommitHistory.builder()
                .hash(revCommit.getName())
                .shortHash(revCommit.abbreviate(7).name())
                .message(revCommit.getFullMessage())
                .authorName(author.getName())
                .authorEmail(author.getEmailAddress())
                .committedAt(author.getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();
    }
}
