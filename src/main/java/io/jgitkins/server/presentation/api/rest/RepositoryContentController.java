package io.jgitkins.server.presentation.api.rest;

import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.FileUploadInfo;
import io.jgitkins.server.application.dto.FileUploadRequest;
import io.jgitkins.server.application.port.in.FileUploadUseCase;
import io.jgitkins.server.application.port.in.FileTreeLoadUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Content", description = "저장소 파일 업로드 및 트리 조회")
@RequestMapping("/api/repositories")
public class RepositoryContentController {

    private final FileUploadUseCase fileUploadUseCase;
    private final FileTreeLoadUseCase fileTreeLoadUseCase;

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
    public ResponseEntity<ApiResponse<String>> uploadFile(@PathVariable String taskCd,
                                                          @PathVariable String repoName,
                                                          @PathVariable String branch,
                                                          @Parameter(schema = @Schema(type = "string", format = "binary"))
                                                          @RequestPart("file") MultipartFile file,
                                                          @RequestPart("request") FileUploadInfo request) throws IOException {
        fileUploadUseCase.uploadFileToRepository(taskCd, repoName, branch, file, request);
        return ApiResponse.ok("File uploaded and committed.");
    }

    @Operation(summary = "View File Tree", description = "트리 조회")
    @GetMapping("/{taskCd}/{repoName}/refs/{branch}/tree")
    public ResponseEntity<ApiResponse<List<FileEntry>>> getTree(@PathVariable String taskCd,
                                                                @PathVariable String repoName,
                                                                @PathVariable String branch,
                                                                @RequestParam(name = "dir", required = false, defaultValue = "") String dir) throws IOException {
        List<FileEntry> files = fileTreeLoadUseCase.getTree(taskCd, repoName, branch, dir);
        return ApiResponse.ok(files);
    }
}
