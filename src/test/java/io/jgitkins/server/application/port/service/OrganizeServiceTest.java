package io.jgitkins.server.application.port.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.result.OrganizeCreationResult;
import io.jgitkins.server.application.mapper.OrganizeApplicationMapper;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Organize;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizeServiceTest {

    @Mock
    private OrganizePort organizePort;

    @Mock
    private UserPort userPort;

    @Mock
    private OrganizeMemberPort organizeMemberPort;

    @Mock
    private CurrentUserPort currentUserPort;

    @Mock
    private OrganizeApplicationMapper organizeApplicationMapper;

    @InjectMocks
    private OrganizeService service;

    @Test
    void createOrganize_savesWhenAvailable() {
        OrganizeCreationCommand command = OrganizeCreationCommand.builder()
                .name("org")
                .ownerId(1L)
                .description("desc")
                .build();

        when(organizePort.findByName(any())).thenReturn(Optional.empty());
        when(userPort.findByUsername("org")).thenReturn(Optional.empty());

        OrganizeCreationResult result = OrganizeCreationResult.builder().name("org").build();
        when(organizePort.save(any(Organize.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(organizeApplicationMapper.toDto(any(Organize.class))).thenReturn(result);

        OrganizeCreationResult response = service.createOrganize(command);

        assertEquals("org", response.getName());
    }
}
