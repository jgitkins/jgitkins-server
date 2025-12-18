package io.jgitkins.server.presentation.api;

import io.jgitkins.server.application.port.in.RepositoryCreationUseCase;
import io.jgitkins.server.application.port.in.RepositoryDeletionUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.in.LoadTreeUseCase;
import io.jgitkins.server.application.port.in.FileUploadUseCase;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.FileUploadRequest;
import io.jgitkins.server.application.dto.RepositoryResult;
import io.jgitkins.server.presentation.dto.CreateRepositoryRequest;
import io.jgitkins.server.presentation.mapper.RepositoryRequestMapper;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Management", description = "저장소 관리")
@RequestMapping("/api/repositories")
public class RepositoryManagementController {

    private final RepositoryCreationUseCase repositoryCreationUseCase;
    private final FileUploadUseCase fileUploadUseCase;
    private final LoadTreeUseCase getTreeUseCase;
    private final RepositoryLoadUseCase repositoryLoadUseCase;
    private final RepositoryDeletionUseCase repositoryDeletionUseCase;
    //    private final UpdateRepositoryUseCase updateRepositoryUseCase;

    private final RepositoryRequestMapper repositoryRequestMapper;

    @Operation(summary = "Create Repository")
    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryResult>> create(@org.springframework.web.bind.annotation.RequestBody CreateRepositoryRequest request) {
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

    @Operation(summary = "File Upload")
    @PostMapping(value = "/{taskCd}/{repoName}/files/{branch}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = FileUploadRequest.class),
                    encoding = @Encoding(name = "request", contentType = "application/json")
            )
    )
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String branch,
            @Parameter(schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") FileUploadInfo request
    ) throws IOException {
        fileUploadUseCase.uploadFileToRepository(taskCd, repoName, branch, file, request);
        return ResponseEntity.ok(ApiResponse.success("File uploaded and committed."));
    }

    @Operation(summary = "View File Tree", description = "트리 조회")
    @GetMapping("/{taskCd}/{repoName}/refs/{branch}/tree")
    public ResponseEntity<ApiResponse<List<FileEntry>>> getTree(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String branch,
            @RequestParam(name = "dir", required = false, defaultValue = "") String dir
    ) throws IOException {
        List<FileEntry> files = getTreeUseCase.getTree(taskCd, repoName, branch, dir);
        return ResponseEntity.ok(ApiResponse.success(files));
    }
}
