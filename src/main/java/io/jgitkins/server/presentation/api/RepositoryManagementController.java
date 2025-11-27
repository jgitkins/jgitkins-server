package io.jgitkins.server.presentation.api;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.port.in.CreateRepositoryUseCase;
import io.jgitkins.server.application.port.in.LoadTreeUseCase;
import io.jgitkins.server.application.port.in.UploadFileUseCase;
import io.jgitkins.server.application.dto.CreateRepositoryCommand;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.FileUploadRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.jgitkins.server.presentation.dto.CreateRepositoryRequest;
import io.jgitkins.server.presentation.mapper.CreateRepositoryMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Management", description = "저장소 관리")
@RequestMapping("/repositories")
public class RepositoryManagementController {

    private final CreateRepositoryUseCase createRepositoryUseCase;
    private final UploadFileUseCase uploadFileUseCase;
    private final LoadTreeUseCase getTreeUseCase;
    private final CreateRepositoryMapper createRepositoryMapper;

    @Operation(summary = "Create Repository")
    @PostMapping
    public ResponseEntity<Void> createBare(@org.springframework.web.bind.annotation.RequestBody CreateRepositoryRequest request) {
        CreateRepositoryCommand command = createRepositoryMapper.toCommand(request);
        createRepositoryUseCase.createBareRepository(command);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<String> uploadFile(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String branch,
            @Parameter(schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") FileUploadInfo request
    ) throws IOException {
        uploadFileUseCase.uploadFileToRepository(taskCd, repoName, branch, file, request);
        return ResponseEntity.ok("File uploaded and committed.");
    }

    @Operation(summary = "View File Tree", description = "트리 조회")
    @GetMapping("/{taskCd}/{repoName}/refs/{branch}/tree")
    public ResponseEntity<List<FileEntry>> getTree(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @PathVariable String branch,
            @RequestParam(name = "dir", required = false, defaultValue = "") String dir
    ) throws IOException {
        List<FileEntry> files = getTreeUseCase.getTree(taskCd, repoName, branch, dir);
        return ResponseEntity.ok(files);
    }
}
