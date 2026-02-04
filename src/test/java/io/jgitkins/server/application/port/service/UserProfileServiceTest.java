package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.service.UserProfileValidator;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserStatus;
import io.jgitkins.server.domain.model.vo.Username;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private UserPort userPort;

    @Mock
    private UserProfileValidator validator;

    @InjectMocks
    private UserProfileService service;

    @Test
    void updateUsername_activatesAndSavesUser() {
        Username requested = Username.from("new_name");
        when(validator.validateUsername("new_name")).thenReturn(requested);
        when(currentUserPort.currentUserId()).thenReturn(Optional.of(1L));
        User pending = User.createWithStatus("temp", "a@b.com", "User", null, UserStatus.PENDING).withId(1L);
        when(userPort.findById(1L)).thenReturn(Optional.of(pending));
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUsername("new_name");

        verify(validator).validateUsernameNotTaken(requested, 1L);
        verify(validator).validateOrganizeNameNotTakenIfCompatible(requested);
        verify(validator).validateUserHasNoRepositories(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userPort).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("new_name", saved.getUsername());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
    }

}
