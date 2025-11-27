package io.jgitkins.server.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.jgitkins.server.application.port.in.CheckMergeabilityUseCase;
import io.jgitkins.server.application.port.in.PerformMergeUseCase;
import io.jgitkins.server.application.dto.MergeRequest;
import io.jgitkins.server.application.dto.MergeResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merge", description = "병합관리")
public class MergeController {

    private final CheckMergeabilityUseCase checkMergeabilityUseCase;
    private final PerformMergeUseCase performMergeUseCase;

    @Operation(summary = "Check Mergeability", description = "소스 브랜치가 타겟 브랜치로 병합 가능한지 확인")
    @GetMapping("/repositories/{taskCd}/{repoName}/merge/check")
    public ResponseEntity<MergeResult> checkMergeability(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @RequestParam String sourceBranch,
            @RequestParam String targetBranch
    ) throws IOException {
        MergeResult result = checkMergeabilityUseCase.checkMergeability(taskCd, repoName, sourceBranch, targetBranch);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Merge", description = "소스 브랜치를 타겟 브랜치로 병합")
    @PostMapping("/repositories/{taskCd}/{repoName}/merge")
    public ResponseEntity<MergeResult> performMerge(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @RequestBody MergeRequest request
    ) throws IOException {
        MergeResult result = performMergeUseCase.performMerge(taskCd, repoName, request);
        return ResponseEntity.ok(result);
    }

}
