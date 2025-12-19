package io.jgitkins.server.presentation.api.rest;

import io.jgitkins.server.application.dto.command.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.port.in.RepositoryCreationUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeletionUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.jgitkins.server.presentation.dto.RepositoryCreateRequest;
import io.jgitkins.server.presentation.mapper.RepositoryRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Management", description = "저장소 관리")
@RequestMapping("/api/repositories")
public class RepositoryManagementController {

    private final RepositoryCreationUseCase repositoryCreationUseCase;
    private final RepositoryLoadUseCase repositoryLoadUseCase;
    private final RepositoryDeletionUseCase repositoryDeletionUseCase;
    //    private final UpdateRepositoryUseCase updateRepositoryUseCase;

    private final RepositoryRequestMapper repositoryRequestMapper;

    @Operation(summary = "Create Repository")
    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryResult>> create(@org.springframework.web.bind.annotation.RequestBody RepositoryCreateRequest request) {
        CreateRepositoryCommand createCommand = repositoryRequestMapper.toCommand(request);
        RepositoryResult result = repositoryCreationUseCase.create(createCommand);
        return ResponseFactory.created(result.getId(), result);
    }

    @Operation(summary = "Get Repository Metadata")
    @GetMapping("/{repositoryId}")
    public ResponseEntity<ApiResponse<RepositoryResult>> getRepository(@PathVariable Long repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(repositoryLoadUseCase.getRepository(repositoryId)));
    }

//    @Operation(summary = "Update Repository Metadata")
//    @PutMapping("/{repositoryId}")
//    public ResponseEntity<ApiResponse<RepositoryResult>> updateRepository(@PathVariable Long repositoryId,
//                                                                          @org.springframework.web.bind.annotation.RequestBody UpdateRepositoryRequest request) {
//        UpdateRepositoryCommand command = createRepositoryMapper.toUpdateCommand(request);
//        RepositoryResult response = updateRepositoryUseCase.updateRepository(repositoryId, command);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    @Operation(summary = "Delete Repository")
    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(@PathVariable Long repositoryId) {
        repositoryDeletionUseCase.deleteRepository(repositoryId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
