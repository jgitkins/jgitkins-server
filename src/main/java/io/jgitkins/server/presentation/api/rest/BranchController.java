package io.jgitkins.server.presentation.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.dto.result.BranchSearchResult;
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
@RequestMapping("/api/repositories/{repositoryId}/branches")
@Tag(name = "Branch Management", description = "브랜치 조회/생성/삭제")
public class BranchController {

    private final BranchLoadUseCase branchLoadUseCase;
    private final BranchCreationUseCase branchCreationUseCase;
    private final BranchDeletetionUseCase branchDeletetionUseCase;
    private final BranchCreateMapper branchCreateMapper;

    @Operation(summary = "Create branch")
    @PostMapping
    public ResponseEntity<Void> create(@PathVariable Long repositoryId,
                                       @RequestBody BranchCreateRequest request) throws IOException {

        BranchCreateCommand createCommand = branchCreateMapper.toCommand(repositoryId, request);
        branchCreationUseCase.createBranch(createCommand);

        URI location = LocationUriBuilder.create(request.getBranchName());
        return ResponseEntity.created(location).build();

    }

    @Operation(summary = "Get Branches")
    @GetMapping
    public ResponseEntity<List<BranchSearchResult>> getBranches(@PathVariable Long repositoryId) {
        return ResponseEntity.ok(branchLoadUseCase.getBranches(repositoryId));
    }

    @Operation(summary = "Get Branch")
    @GetMapping("/{branchName}")
    public ResponseEntity<BranchSearchResult> getBranch(@PathVariable Long repositoryId, @PathVariable String branchName) throws IOException {
        return ResponseEntity.ok(branchLoadUseCase.getBranch(repositoryId, branchName));
    }

    @Operation(summary = "Delete branch")
    @DeleteMapping("/{branchName}")
    public ResponseEntity<Void> deleteBranch(@PathVariable String organizeId,
                                             @PathVariable String repositoryId,
                                             @PathVariable String branchName) throws IOException {

        branchDeletetionUseCase.deleteBranch(organizeId, repositoryId, branchName);
        return ResponseEntity.noContent().build();
    }
}
