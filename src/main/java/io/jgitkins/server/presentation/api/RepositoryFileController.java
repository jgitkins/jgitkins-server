package io.jgitkins.server.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.port.in.LoadAllFilesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Files", description = "저장소 파일 조회")
@RequestMapping("/repositories/{taskCd}/{repoName}/files")
public class RepositoryFileController {

    private final LoadAllFilesUseCase getAllFilesUseCase;

    @Operation(summary = "List Repository Files", description = "지정한 참조(브랜치/커밋)의 전체 파일 목록 조회")
    @GetMapping
    public ResponseEntity<List<FileEntry>> listFiles(
            @PathVariable String taskCd,
            @PathVariable String repoName,
            @RequestParam(name = "ref", required = false, defaultValue = "") String ref
    ) {
        List<FileEntry> files = getAllFilesUseCase.getAllFiles(taskCd, repoName, ref);
        return ResponseEntity.ok(files);
    }
}
