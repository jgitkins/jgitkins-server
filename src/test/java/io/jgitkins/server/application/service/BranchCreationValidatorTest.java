package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.dto.command.BranchCreateCommand;
import io.jgitkins.server.application.port.out.BranchPort;
import io.jgitkins.server.domain.Branch;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.BranchName;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchCreationValidatorTest {

    @Mock
    private BranchPort branchPort;

    @InjectMocks
    private BranchCreationValidator validator;

    @Test
    void validateBranchDoesNotExist_throwsWhenBranchAlreadyExists() throws IOException {
        when(branchPort.getBranch(1L, "feature")).thenReturn(Optional.of(Branch.create(1L, "feature")));

        assertThrows(ConflictException.class, () -> validator.validateBranchDoesNotExist(1L, "feature"));
    }

    @Test
    void validateRepositoryInitialized_throwsWhenRepositoryNotInitialized() {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        when(repository.isInitialized()).thenReturn(false);

        assertThrows(UnprocessableException.class, () -> validator.validateRepositoryInitialized(repository));
    }

    @Test
    void resolveAndValidateSourceBranch_usesDefaultBranchWhenSourceMissing() throws IOException {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        when(repository.getId()).thenReturn(RepositoryId.of(1L));
        when(repository.getDefaultBranch()).thenReturn(BranchName.of("main"));
        when(branchPort.getBranch(1L, "main")).thenReturn(Optional.of(Branch.create(1L, "main")));

        BranchCreateCommand command = BranchCreateCommand.builder()
                .repositoryId(1L)
                .branchName("feature")
                .sourceBranch(null)
                .build();

        String sourceBranch = validator.resolveAndValidateSourceBranch(command, repository);

        assertEquals("main", sourceBranch);
    }

    @Test
    void resolveAndValidateSourceBranch_throwsWhenSourceBranchMissing() throws IOException {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        when(repository.getId()).thenReturn(RepositoryId.of(1L));
        when(branchPort.getBranch(1L, "dev")).thenReturn(Optional.empty());

        BranchCreateCommand command = BranchCreateCommand.builder()
                .repositoryId(1L)
                .branchName("feature")
                .sourceBranch("dev")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> validator.resolveAndValidateSourceBranch(command, repository));
    }

    @Test
    void validateNotDefaultBranch_throwsWhenDeletingDefaultBranch() {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        Branch defaultBranch = Branch.create(1L, "main", false, false, true);
        when(repository.getDefaultBranch()).thenReturn(BranchName.of("main"));

        assertThrows(ConflictException.class, () -> validator.validateNotDefaultBranch(repository, defaultBranch));
    }
}
