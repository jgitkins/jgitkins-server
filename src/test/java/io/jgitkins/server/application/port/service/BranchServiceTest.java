package io.jgitkins.server.application.port.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.port.out.BranchGitPort;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.mapper.BranchApplicationMapper;
import io.jgitkins.server.application.service.BranchCreationValidator;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.application.service.RepositoryUploadPermissionGuard;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import io.jgitkins.server.domain.model.vo.RepositoryName;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private RepositoryNamespaceResolver repositoryNamespaceResolver;

    @Mock
    private BranchCreationValidator branchCreationValidator;

    @Mock
    private RepositoryUploadPermissionGuard repositoryWritePermissionGuard;

    @Mock
    private BranchApplicationMapper branchApplicationMapper;

    @Mock
    private BranchGitPort branchGitPort;

    @Mock
    private BranchPort branchPort;

    @Mock
    private RepositoryPort repositoryPort;

    @InjectMocks
    private BranchService service;

    @Test
    void createBranch_createsBranchInGitAndPersistence() {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        when(repository.getName()).thenReturn(RepositoryName.from("repo"));
        when(repositoryPort.findById(RepositoryId.of(1L))).thenReturn(Optional.of(repository));
        when(repositoryNamespaceResolver.resolve(repository)).thenReturn("org");

        BranchCreateCommand command = BranchCreateCommand.builder()
                .repositoryId(1L)
                .branchName("feature")
                .sourceBranch("main")
                .build();

        service.createBranch(command);

        verify(repositoryWritePermissionGuard).assertCanWrite(repository);
        verify(branchGitPort).createBranch(any());
        ArgumentCaptor<io.jgitkins.server.domain.Branch> captor = ArgumentCaptor.forClass(
                io.jgitkins.server.domain.Branch.class);
        verify(branchPort).create(captor.capture());
        io.jgitkins.server.domain.Branch created = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(1L, created.getRepositoryId());
        org.junit.jupiter.api.Assertions.assertEquals("feature", created.getName());
    }

    @Test
    void deleteBranch_deletesInGitAndPersistenceWhenNotDefaultBranch() {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        Branch branch = Branch.create(1L, "feature");

        when(repository.getName()).thenReturn(RepositoryName.from("repo"));
        when(repositoryPort.findById(RepositoryId.of(1L))).thenReturn(Optional.of(repository));
        when(repositoryNamespaceResolver.resolve(repository)).thenReturn("org");
        when(branchPort.getBranch(1L, "feature")).thenReturn(Optional.of(branch));

        service.deleteBranch(1L, "feature");

        verify(repositoryWritePermissionGuard).assertCanWrite(repository);
        verify(branchCreationValidator).validateNotDefaultBranch(repository, branch);
        verify(branchGitPort).deleteBranch("org", "repo", "feature");
        verify(branchPort).delete(1L, "feature");
    }

    @Test
    void getBranch_throwsWhenBranchMissing() {
        when(branchPort.getBranch(1L, "missing")).thenReturn(Optional.empty());

        assertThrows(JgitkinsException.class, () -> service.getBranch(1L, "missing"));
    }
}
