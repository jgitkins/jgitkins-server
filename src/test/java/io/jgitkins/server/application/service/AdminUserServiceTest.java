package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.result.UserAdminDetail;
import io.jgitkins.server.application.dto.result.UserAdminSummary;
import io.jgitkins.server.application.port.out.UserIdentityPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserAuthority;
import io.jgitkins.server.domain.model.UserIdentity;
import io.jgitkins.server.domain.model.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserPort userPort;

    @Mock
    private UserIdentityPort userIdentityPort;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void getUsers_mapsDomainUsersToSummaries() {
        User user = User.rehydrate(
                1L,
                "alice",
                "alice@example.com",
                "Alice",
                null,
                UserAuthority.USER,
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(userPort.findAll()).thenReturn(List.of(user));

        List<UserAdminSummary> summaries = adminUserService.getUsers();

        assertEquals(1, summaries.size());
        assertEquals("alice", summaries.get(0).getUsername());
        assertEquals("ACTIVE", summaries.get(0).getStatus());
    }

    @Test
    void getUser_returnsDetailWithIdentities() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.rehydrate(
                2L,
                "bob",
                "bob@example.com",
                "Bob",
                "https://img/bob.png",
                UserAuthority.USER,
                UserStatus.BLOCKED,
                now,
                now,
                now
        );
        UserIdentity identity = UserIdentity.rehydrate(
                100L,
                2L,
                "google",
                "sub-2",
                "bob@example.com",
                true,
                "Bob",
                "https://img/bob.png",
                now,
                now
        );

        when(userPort.findById(2L)).thenReturn(Optional.of(user));
        when(userIdentityPort.findAllByUserId(2L)).thenReturn(List.of(identity));

        UserAdminDetail detail = adminUserService.getUser(2L);

        assertEquals(2L, detail.getId());
        assertEquals("BLOCKED", detail.getStatus());
        assertEquals(1, detail.getIdentities().size());
        assertEquals("google", detail.getIdentities().get(0).getProviderName());
    }

    @Test
    void updateUserStatus_updatesWhenValid() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.rehydrate(
                3L,
                "carol",
                "carol@example.com",
                "Carol",
                null,
                UserAuthority.USER,
                UserStatus.ACTIVE,
                now,
                now,
                now
        );
        when(userPort.findById(3L)).thenReturn(Optional.of(user));
        when(userPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.updateUserStatus(3L, "blocked");

        verify(userPort).save(any(User.class));
    }

    @Test
    void updateUserStatus_throwsWhenStatusInvalid() {
        assertThrows(IllegalArgumentException.class, () -> adminUserService.updateUserStatus(1L, "UNKNOWN"));
    }
}
