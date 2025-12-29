package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.common.exception.UnprocessableException;
import io.jgitkins.server.application.dto.command.RepositoryCreateCommand;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.RepositoryGitPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.application.common.event.DomainEventPublisher;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.OwnerType;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryLifecycleServiceTest {

    private static final String REPO_NAME = "my-repo";
    private static final String MAIN_BRANCH = "main";
    private static final Long CURRENT_USER_ID = 7L;
    private static final String CURRENT_USERNAME = "alice";

    @Mock
    private RepositoryApplicationMapper repositoryApplicationMapper;
    @Mock
    private DomainEventPublisher domainEventPublisher;
    @Mock
    private RepositoryGitPort repositoryGitPort;
    @Mock
    private RepositoryPort repositoryPort;
    @Mock
    private OrganizeMemberPort organizeMemberPort;
    @Mock
    private RepositoryNamespaceResolver repositoryNamespaceResolver;
    @Mock
    private CurrentUserPort currentUserPort;

    private RepositoryLifecycleService repositoryLifecycleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repositoryLifecycleService = new RepositoryLifecycleService(
                repositoryApplicationMapper,
                domainEventPublisher,
                repositoryGitPort,
                repositoryPort,
                organizeMemberPort,
                repositoryNamespaceResolver,
                currentUserPort
        );
    }

    @Test
    void shouldCreateUserRepositoryWhenOwnerTypeIsUser() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("USER")
                .build();
        givenAuthenticatedUser();
        givenUserNamespaceResolved();
        givenRepositoryDoesNotExist();
        givenRepositorySaved(1L);
        givenMapperResult(1L);

        // when
        repositoryLifecycleService.create(command);

        // then
        ArgumentCaptor<Repository> repositoryCaptor = ArgumentCaptor.forClass(Repository.class);
        verify(repositoryPort).save(repositoryCaptor.capture());
        Repository saved = repositoryCaptor.getValue();
        assertThat(saved.getOwnerType()).isEqualTo(OwnerType.USER);
        assertThat(saved.getOwnerId().getValue()).isEqualTo(CURRENT_USER_ID);
        assertThat(saved.getName().getValue()).isEqualTo(REPO_NAME);
        assertThat(saved.getClonePath()).isEqualTo("/users/" + CURRENT_USERNAME + "/" + REPO_NAME + ".git");
        verify(repositoryGitPort).create("users/" + CURRENT_USERNAME, REPO_NAME);
    }

    @Test
    void shouldCreateOrganizationRepositoryWhenOwnerTypeIsOrganization() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("ORGANIZATION")
                .organizeId(55L)
                .build();
        givenAuthenticatedUser();
        givenUserIsOrganizeMember(55L);
        givenRepositoryDoesNotExist();
        givenOrganizationNamespaceResolved("dev-team");
        givenRepositorySaved(2L);
        givenMapperResult(2L);

        // when
        repositoryLifecycleService.create(command);

        // then
        ArgumentCaptor<Repository> repositoryCaptor = ArgumentCaptor.forClass(Repository.class);
        verify(repositoryPort).save(repositoryCaptor.capture());
        Repository saved = repositoryCaptor.getValue();
        assertThat(saved.getOwnerType()).isEqualTo(OwnerType.ORGANIZATION);
        assertThat(saved.getOwnerId().getValue()).isEqualTo(55L);
        assertThat(saved.getName().getValue()).isEqualTo(REPO_NAME);
        assertThat(saved.getClonePath()).isEqualTo("/dev-team/my-repo.git");
        verify(repositoryGitPort).create("dev-team", REPO_NAME);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidOwnerSelections")
    void shouldRejectInvalidOwnerSelection(String description, RepositoryCreateCommand command, String expectedMessage) {
        // when + then
        assertThatThrownBy(() -> repositoryLifecycleService.create(command))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    void shouldRejectWhenRepositoryNameAlreadyExistsForOwner() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("USER")
                .build();
        givenAuthenticatedUser();
        givenUserNamespaceResolved();
        when(repositoryPort.findByOwnerAndName(any(), any(), any())).thenReturn(Optional.of(mock(Repository.class)));

        // when + then
        assertThatThrownBy(() -> repositoryLifecycleService.create(command))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Repository name already exists");
    }

    @Test
    void shouldRejectWhenUserUnauthenticated() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("USER")
                .build();
        when(currentUserPort.currentUserId()).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> repositoryLifecycleService.create(command))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("Unauthenticated");
    }

    @Test
    void shouldRejectWhenOrganizationNotFound() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("ORGANIZATION")
                .organizeId(44L)
                .build();
        givenAuthenticatedUser();
        givenUserIsOrganizeMember(44L);
        givenRepositoryDoesNotExist();
        givenOrganizationNamespaceMissing();

        // when + then
        assertThatThrownBy(() -> repositoryLifecycleService.create(command))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Organize not found");
    }

    @Test
    void shouldRejectWhenUserIsNotOrganizationMember() {
        // given
        RepositoryCreateCommand command = baseCommand()
                .ownerType("ORGANIZATION")
                .organizeId(21L)
                .build();
        givenAuthenticatedUser();
        givenUserIsNotOrganizeMember();

        // when + then
        assertThatThrownBy(() -> repositoryLifecycleService.create(command))
                .isInstanceOf(UnprocessableException.class)
                .hasMessageContaining("User is not a member");
    }

    private static Stream<Arguments> invalidOwnerSelections() {
        return Stream.of(
                Arguments.of(
                        "USER ownerType with organizeId should fail",
                        baseCommand()
                                .ownerType("USER")
                                .organizeId(1L)
                                .build(),
                        "organizeId must be null"
                ),
                Arguments.of(
                        "ORGANIZATION ownerType without organizeId should fail",
                        baseCommand()
                                .ownerType("ORGANIZATION")
                                .build(),
                        "organizeId is required"
                )
        );
    }

    private static RepositoryCreateCommand.RepositoryCreateCommandBuilder baseCommand() {
        return RepositoryCreateCommand.builder()
                .repoName(REPO_NAME)
                .mainBranch(MAIN_BRANCH);
    }

    private void givenAuthenticatedUser() {
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(CURRENT_USER_ID));
    }

    private void givenRepositoryDoesNotExist() {
        when(repositoryPort.findByOwnerAndName(any(), any(), any())).thenReturn(Optional.empty());
    }

    private void givenUserIsOrganizeMember(Long organizeId) {
        when(organizeMemberPort.existsByOrganizeAndUser(any(), any())).thenReturn(true);
    }

    private void givenUserIsNotOrganizeMember() {
        when(organizeMemberPort.existsByOrganizeAndUser(any(), any())).thenReturn(false);
    }

    private void givenUserNamespaceResolved() {
        when(repositoryNamespaceResolver.resolve(any(), any()))
                .thenReturn("users/" + CURRENT_USERNAME);
    }

    private void givenOrganizationNamespaceResolved(String name) {
        when(repositoryNamespaceResolver.resolve(any(), any())).thenReturn(name);
    }

    private void givenOrganizationNamespaceMissing() {
        when(repositoryNamespaceResolver.resolve(any(), any()))
                .thenThrow(new ResourceNotFoundException(
                        io.jgitkins.server.application.common.ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: 44"));
    }

    private void givenRepositorySaved(Long repositoryId) {
        when(repositoryPort.save(any())).thenAnswer(invocation -> {
            Repository repository = invocation.getArgument(0);
            return repository.withIdentity(RepositoryId.of(repositoryId), LocalDateTime.now(), LocalDateTime.now());
        });
        when(repositoryPort.findById(any())).thenReturn(Optional.empty());
    }

    private void givenMapperResult(Long repositoryId) {
        when(repositoryApplicationMapper.toDto(any())).thenReturn(RepositoryResult.builder().id(repositoryId).build());
    }
}
