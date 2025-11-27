package io.jgitkins.server.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.CommitHistory;
import io.jgitkins.server.application.port.in.LoadBranchCommitHistoriesUseCase;
import io.jgitkins.server.application.port.in.LoadCommitDetailUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Commit", description = "커밋 관리")
@RequestMapping("/repositories")
public class RepositoryCommitController {

    private final LoadCommitDetailUseCase loadCommitDetailUseCase;
    private final LoadBranchCommitHistoriesUseCase loadBranchCommitHistoriesUseCase;

    @Operation(summary = "View Commit Detail", description = "커밋 상세 조회")
    @GetMapping("/{taskCd}/{repoName}/commits/{commitHash}")
    public ResponseEntity<CommitHistory> getCommitDetail(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String commitHash
    ) throws IOException {
        CommitHistory commitHistory = loadCommitDetailUseCase.getCommitDetail(taskCd, repoName, commitHash);
        return ResponseEntity.ok(commitHistory);
    }

    @Operation(summary = "View Commit Histories", description = "커밋 이력 조회")
    @GetMapping("/{taskCd}/{repoName}/branches/{branch}/commits")
    public ResponseEntity<List<CommitHistory>> getBranchCommitHistories(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String branch
    ) throws IOException {
        List<CommitHistory> commitHistories = loadBranchCommitHistoriesUseCase.getBranchCommitHistories(taskCd, repoName, branch);
        return ResponseEntity.ok(commitHistories);
    }
}
