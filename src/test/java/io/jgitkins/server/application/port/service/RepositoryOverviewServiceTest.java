package io.jgitkins.server.application.port.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.result.BranchSearchResult;
import io.jgitkins.server.application.dto.result.RepositoryOverviewResult;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.application.port.in.FileTreeLoadUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryOverviewServiceTest {

    @Mock
    private RepositoryLoadUseCase repositoryLoadUseCase;

    @Mock
    private BranchLoadUseCase branchLoadUseCase;

    @Mock
    private FileTreeLoadUseCase fileTreeLoadUseCase;

    @InjectMocks
    private RepositoryOverviewService service;

    @Test
    void getOverview_usesDefaultBranchAndLoadsTree() throws IOException {
        RepositoryResult repository = RepositoryResult.builder()
                .clonePath("org/repo.git")
                .path("org/repo")
                .build();
        when(repositoryLoadUseCase.getRepository(1L)).thenReturn(repository);

        List<BranchSearchResult> branches = List.of(
                BranchSearchResult.builder().name("main").defaultBranch(true).build());
        when(branchLoadUseCase.getBranches(1L)).thenReturn(branches);

        List<FileEntry> tree = List.of(FileEntry.builder().name("README.md").build());
        when(fileTreeLoadUseCase.getTree("org", "repo", "main", "")).thenReturn(tree);

        RepositoryOverviewResult result = service.getOverview(1L, null);

        assertEquals("main", result.getSelectedBranch());
        assertEquals(tree, result.getTree());
    }
}
