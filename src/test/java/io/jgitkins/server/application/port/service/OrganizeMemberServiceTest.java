package io.jgitkins.server.application.port.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.OrganizeMemberAddCommand;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizeMemberServiceTest {

    @Mock
    private OrganizeMemberPort organizeMemberPort;

    @InjectMocks
    private OrganizeMemberService service;

    @Test
    void addOrganizeMember_savesWhenNotExists() {
        when(organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(1L), UserId.of(2L))).thenReturn(false);

        OrganizeMemberAddCommand command = OrganizeMemberAddCommand.builder()
                .organizeId(1L)
                .userId(2L)
                .build();

        service.addOrganizeMember(command);

        verify(organizeMemberPort).save(org.mockito.ArgumentMatchers.any());
    }
}
