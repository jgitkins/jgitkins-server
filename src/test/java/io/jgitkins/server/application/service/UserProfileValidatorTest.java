package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import io.jgitkins.server.common.exception.JgitkinsException;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.RepositoryPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.vo.OwnerId;
import io.jgitkins.server.domain.model.vo.OwnerType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileValidatorTest {

    @Mock
    private UserPort userPort;

    @Mock
    private OrganizePort organizePort;

    @Mock
    private RepositoryPort repositoryPort;

    @InjectMocks
    private UserProfileValidator validator;

    @Test
    void validateUsername_throwsWhenInvalid() {
        assertThrows(JgitkinsException.class, () -> validator.validateUsername("bad name"));
    }

    @Test
    void validateUserHasNoRepositories_throwsWhenOwnedRepositoriesExist() {
        when(repositoryPort.countByOwner(OwnerType.USER, OwnerId.of(1L))).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> validator.validateUserHasNoRepositories(1L));
    }
}
