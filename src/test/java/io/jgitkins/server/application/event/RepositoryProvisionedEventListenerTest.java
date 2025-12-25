//package io.jgitkins.server.application.event;
//
//import io.jgitkins.server.application.common.event.DomainEventPublisher;
//import io.jgitkins.server.application.dto.CommitFile;
//import io.jgitkins.server.application.port.out.*;
//import io.jgitkins.server.domain.aggregate.Organize;
//import io.jgitkins.server.domain.aggregate.Repository;
//import io.jgitkins.server.domain.event.RepositoryProvisionedEvent;
//import io.jgitkins.server.domain.model.vo.BranchName;
//import io.jgitkins.server.domain.model.vo.InitialCommitOptions;
//import io.jgitkins.server.domain.model.vo.OrganizeId;
//import io.jgitkins.server.domain.model.vo.OrganizeName;
//import io.jgitkins.server.domain.model.vo.RepositoryName;
//import io.jgitkins.server.domain.model.vo.RepositoryPath;
//import io.jgitkins.server.domain.model.vo.RepositoryVisibility;
//import io.jgitkins.server.domain.model.vo.UserId;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class RepositoryProvisionedEventListenerTest {
//
//    @Mock
//    private OrganizePort organizePort;
//    @Mock
//    private FileGitPort repositoryContentPort;
//    @Mock
//    private CommitGitPort repositoryCommitPort;
//    @Mock
//    private RepositoryGitPort updateHeadReferencePort;
//    @Mock
//    private RepositoryPort repositoryPort;
//    @Mock
//    private DomainEventPublisher domainEventPublisher;
//
//    @InjectMocks
//    private RepositoryProvisionedEventListener listener;
//
//    @Test
//    void shouldHandleInitialProvisioningWhenReadmeRequested() {
//        InitialCommitOptions options = InitialCommitOptions.of(true, "Initial commit", "Alice", "alice@example.com");
//        Repository repository = Repository.create(
//                OrganizeId.of(1L),
//                RepositoryName.from("demo"),
//                RepositoryPath.from("demo"),
//                BranchName.of("main"),
//                RepositoryVisibility.PRIVATE,
//                UserId.of(10L),
//                null,
//                "/org/demo.git",
//                null,
//                options
//        );
//        RepositoryProvisionedEvent event = RepositoryProvisionedEvent.from(repository, options);
//
//        Organize organize = Organize.reconstruct(
//                OrganizeId.of(1L),
//                OrganizeName.from("org"),
//                null,
//                null,
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//        when(organizePort.findById(OrganizeId.of(1L))).thenReturn(Optional.of(organize));
//        List<CommitFile> files = List.of(new CommitFile("README.md", "hello".getBytes()));
//        when(repositoryContentPort.prepareInitialFile("demo")).thenReturn(files);
//        when(repositoryPort.findByOrganizeAndName(OrganizeId.of(1L), RepositoryName.from("demo")))
//                .thenReturn(Optional.of(repository));
//        Repository synced = repository.markInit(LocalDateTime.now());
//        when(repositoryPort.update(any(Repository.class))).thenReturn(synced);
//
//        listener.onRepositoryProvisioned(event);
//
//        verify(repositoryCommitPort).commit("org",
//                                            "demo",
//                                            "main",
//                                            "Initial commit",
//                                            "Alice",
//                                            "alice@example.com",
//                                            files);
//        verify(updateHeadReferencePort).updateHeadReference("org", "demo", "main");
//        verify(repositoryPort).update(any(Repository.class));
//        verify(domainEventPublisher).publish(anyList());
//    }
//
//    @Test
//    void shouldSkipWhenInitialContentNotRequired() {
//        InitialCommitOptions options = InitialCommitOptions.of(false, null, null, null);
//        Repository repository = Repository.create(
//                OrganizeId.of(2L),
//                RepositoryName.from("demo"),
//                RepositoryPath.from("demo"),
//                BranchName.of("main"),
//                RepositoryVisibility.PRIVATE,
//                null,
//                null,
//                "/org/demo.git",
//                null,
//                options
//        );
//        RepositoryProvisionedEvent event = RepositoryProvisionedEvent.from(repository, options);
//
//        Organize organize = Organize.reconstruct(
//                OrganizeId.of(2L),
//                OrganizeName.from("org"),
//                null,
//                null,
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//        when(organizePort.findById(OrganizeId.of(2L))).thenReturn(Optional.of(organize));
//        when(repositoryPort.findByOrganizeAndName(OrganizeId.of(2L), RepositoryName.from("demo")))
//                .thenReturn(Optional.of(repository));
//        Repository synced = repository.markInit(LocalDateTime.now());
//        when(repositoryPort.update(any(Repository.class))).thenReturn(synced);
//
//        listener.onRepositoryProvisioned(event);
//
//        verify(repositoryContentPort, never()).prepareInitialFile(any());
//        verify(repositoryCommitPort, never()).commit(any(), any(), any(), any(), any(), any(), any());
//        verify(updateHeadReferencePort).updateHeadReference("org", "demo", "main");
//        verify(repositoryPort).update(any(Repository.class));
//        verify(domainEventPublisher).publish(anyList());
//    }
//}
