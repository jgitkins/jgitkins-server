package io.jgitkins.server.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.BranchCreateCommand;
import io.jgitkins.server.application.dto.BranchInfo;
import io.jgitkins.server.application.port.in.BranchCreationUseCase;
import io.jgitkins.server.application.port.in.BranchDeletetionUseCase;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.presentation.dto.BranchCreateRequest;
import io.jgitkins.server.presentation.mapper.BranchCreateMapper;
import io.jgitkins.server.presentation.util.LocationUriBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/repositories/{taskCd}/{repoName}/branches")
@Tag(name = "Branch Management", description = "브랜치 조회/생성/삭제")
public class BranchController {

    private final BranchLoadUseCase branchLoadUseCase;
    private final BranchCreationUseCase branchCreationUseCase;
    private final BranchDeletetionUseCase branchDeletetionUseCase;
    private final BranchCreateMapper branchCreateMapper;

    @Operation(summary = "List branches")
    @GetMapping
    public ResponseEntity<List<BranchInfo>> getBranches(@PathVariable String taskCd,
                                                        @PathVariable String repoName) throws IOException {
        return ResponseEntity.ok(branchLoadUseCase.getBranches(taskCd, repoName));
    }

    @Operation(summary = "Create branch")
    @PostMapping
    public ResponseEntity<Void> createBranch(@PathVariable String taskCd,
                                             @PathVariable String repoName,
                                             @RequestBody BranchCreateRequest request) throws IOException {

        BranchCreateCommand command = branchCreateMapper.toCommand(taskCd, repoName, request);
        branchCreationUseCase.createBranch(command);

        URI location = LocationUriBuilder.create(request.getBranchName());
        return ResponseEntity.created(location).build();

    }

    @Operation(summary = "Delete branch")
    @DeleteMapping("/{branchName}")
    public ResponseEntity<Void> deleteBranch(@PathVariable String taskCd,
                                             @PathVariable String repoName,
                                             @PathVariable String branchName) throws IOException {

        branchDeletetionUseCase.deleteBranch(taskCd, repoName, branchName);
        return ResponseEntity.noContent().build();
    }
}
