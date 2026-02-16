package io.jgitkins.server.presentation.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.port.in.FileLoadUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Repository Files", description = "저장소 파일 조회")
@RequestMapping("/repositories/{taskCd}/{repoName}/files")
public class RepositoryFileController {

    private final FileLoadUseCase fileLoadUseCase;

    @Operation(summary = "List Repository Files", description = "지정한 참조(브랜치/커밋)의 전체 파일 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FileEntry>>> listFiles(@PathVariable String taskCd,
                                                                  @PathVariable String repoName,
                                                                  @RequestParam(name = "ref", required = false, defaultValue = "") String ref) {

        List<FileEntry> files = fileLoadUseCase.getAllFiles(taskCd, repoName, ref);
        return ApiResponse.ok(files);
    }

    @Operation(summary = "Search Repository Files", description = "브랜치 기준 전체 파일 목록에서 키워드 검색")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FileEntry>>> searchFiles(@PathVariable String taskCd,
                                                                     @PathVariable String repoName,
                                                                     @RequestParam(name = "ref", required = false, defaultValue = "") String ref,
                                                                     @RequestParam(name = "q", required = false, defaultValue = "") String query,
                                                                     @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 200));
        String normalized = query == null ? "" : query.trim().toLowerCase();

        List<FileEntry> files = fileLoadUseCase.getAllFiles(taskCd, repoName, ref).stream()
                .filter(file -> {
                    if (normalized.isEmpty()) {
                        return true;
                    }
                    String name = file.getName() == null ? "" : file.getName().toLowerCase();
                    String path = file.getPath() == null ? "" : file.getPath().toLowerCase();
                    return name.contains(normalized) || path.contains(normalized);
                })
                .sorted(Comparator.comparing(FileEntry::getPath, Comparator.nullsLast(String::compareToIgnoreCase)))
                .limit(safeLimit)
                .toList();

        return ApiResponse.ok(files);
    }
}
