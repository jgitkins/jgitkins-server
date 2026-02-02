package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.port.out.UserIdentityPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.service.support.UserProfileUpdater;
import io.jgitkins.server.application.service.support.UsernameAllocator;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserIdentity;
import io.jgitkins.server.domain.model.UserStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    void loginOrSignUp_throwsWhenProviderIdentityMissing() {
        UserService service = new UserService(mock(UserPort.class), mock(UserIdentityPort.class),
                mock(UsernameAllocator.class), new UserProfileUpdater());

        assertThrows(IllegalArgumentException.class, () ->
                service.loginOrSignUp(null, "sub", null, false, null, null));
    }

    @Test
    void loginOrSignUp_signsInExistingIdentity() {
        UserPort userPort = mock(UserPort.class);
        UserIdentityPort identityPort = mock(UserIdentityPort.class);
        UsernameAllocator allocator = mock(UsernameAllocator.class);
        UserProfileUpdater updater = new UserProfileUpdater();

        User user = User.createWithStatus("user", "a@b.com", "User", null, UserStatus.ACTIVE).withId(1L);
        UserIdentity identity = UserIdentity.rehydrate(10L, 1L, "google", "sub", "a@b.com", true, "User", null,
                user.getCreatedAt(), user.getUpdatedAt());

        when(identityPort.findByProvider("google", "sub")).thenReturn(Optional.of(identity));
        when(userPort.findById(1L)).thenReturn(Optional.of(user));
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserService service = new UserService(userPort, identityPort, allocator, updater);

        User result = service.loginOrSignUp("google", "sub", "a@b.com", true, "User", null);

        assertEquals(1L, result.getId());
        verify(identityPort, never()).save(any(UserIdentity.class));
    }

    @Test
    void loginOrSignUp_signsUpWhenIdentityMissing() {
        UserPort userPort = mock(UserPort.class);
        UserIdentityPort identityPort = mock(UserIdentityPort.class);
        UsernameAllocator allocator = mock(UsernameAllocator.class);
        UserProfileUpdater updater = new UserProfileUpdater();

        when(identityPort.findByProvider("google", "sub")).thenReturn(Optional.empty());
        when(allocator.deriveBaseUsername(any(), any(), any())).thenReturn("base");
        when(allocator.allocateUniqueUsername("base", "sub")).thenReturn("unique");
        when(userPort.findByEmail(any())).thenReturn(Optional.empty());
        when(userPort.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user.withId(2L);
        });

        UserService service = new UserService(userPort, identityPort, allocator, updater);

        User result = service.loginOrSignUp("google", "sub", "a@b.com", true, "User", null);

        assertEquals(2L, result.getId());
        verify(identityPort).save(any(UserIdentity.class));
    }
}
