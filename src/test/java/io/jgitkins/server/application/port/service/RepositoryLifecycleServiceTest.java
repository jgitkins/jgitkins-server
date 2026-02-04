package io.jgitkins.server.application.port.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.mapper.RepositoryApplicationMapper;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.RepositoryGitPort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.service.RepositoryNamespaceResolver;
import io.jgitkins.server.domain.aggregate.Repository;
import io.jgitkins.server.domain.model.vo.RepositoryId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryLifecycleServiceTest {

    @Mock
    private RepositoryNamespaceResolver repositoryNamespaceResolver;

    @Mock
    private RepositoryApplicationMapper repositoryApplicationMapper;

    @Mock
    private io.jgitkins.server.application.common.event.DomainEventPublisher domainEventPublisher;

    @Mock
    private RepositoryGitPort repositoryGitPort;

    @Mock
    private RepositoryPort repositoryPort;

    @Mock
    private OrganizeMemberPort organizeMemberPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private UserPort userPort;

    @InjectMocks
    private RepositoryLifecycleService service;

    @Test
    void getRepository_returnsMappedResult() {
        Repository repository = org.mockito.Mockito.mock(Repository.class);
        when(repositoryPort.findById(RepositoryId.of(1L))).thenReturn(Optional.of(repository));
        RepositoryResult result = RepositoryResult.builder().id(1L).build();
        when(repositoryApplicationMapper.toDto(repository)).thenReturn(result);

        RepositoryResult response = service.getRepository(1L);

        assertEquals(1L, response.getId());
    }
}
