package io.jgitkins.server.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.result.UserSummary;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicUserQueryServiceTest {

    @Mock
    private UserPort userPort;

    @InjectMocks
    private PublicUserQueryService publicUserQueryService;

    @Test
    void getUsers_mapsDomainUsersToSummaries() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        User alice = User.rehydrate(
                1L,
                "alice",
                "alice@example.com",
                "Alice",
                "https://img/alice.png",
                UserStatus.ACTIVE,
                createdAt,
                createdAt,
                createdAt
        );

        when(userPort.findAll()).thenReturn(List.of(alice));

        List<UserSummary> result = publicUserQueryService.getUsers();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("Alice", result.get(0).getDisplayName());
        assertEquals("https://img/alice.png", result.get(0).getAvatarUrl());
        assertEquals(createdAt, result.get(0).getCreatedAt());
        verify(userPort).findAll();
    }
}
