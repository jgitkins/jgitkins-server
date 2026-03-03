package io.jgitkins.server.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UsernameAllocatorTest {

    @Test
    void allocateUniqueUsername_returnsBaseWhenAvailable() {
        UserPort userPort = mock(UserPort.class);
        OrganizePort organizePort = mock(OrganizePort.class);
        when(userPort.findByUsername(anyString())).thenReturn(Optional.empty());
        when(organizePort.findByName(any(OrganizeName.class))).thenReturn(Optional.empty());

        UsernameAllocator allocator = new UsernameAllocator(userPort, organizePort);

        String result = allocator.allocateUniqueUsername("base", "provider-sub");

        assertEquals("base", result);
    }

    @Test
    void allocateUniqueUsername_fallsBackToProviderSuffixWhenBaseTaken() {
        UserPort userPort = mock(UserPort.class);
        OrganizePort organizePort = mock(OrganizePort.class);
        when(userPort.findByUsername(anyString())).thenAnswer(invocation -> {
            String value = invocation.getArgument(0);
            if ("base".equals(value)) {
                return Optional.of(User.create("base", null, null, null));
            }
            return Optional.empty();
        });
        when(organizePort.findByName(any(OrganizeName.class))).thenReturn(Optional.empty());

        UsernameAllocator allocator = new UsernameAllocator(userPort, organizePort);

        String result = allocator.allocateUniqueUsername("base", "ABCDEF123456");

        assertEquals("base-123456", result);
    }
}
