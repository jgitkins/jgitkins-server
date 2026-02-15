package io.jgitkins.server.application.port.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.dto.command.OrganizeMemberAddCommand;
import io.jgitkins.server.application.dto.result.OrganizeMemberSummary;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeMemberRole;
import io.jgitkins.server.domain.model.vo.UserId;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
                .role(OrganizeMemberRole.OWNER)
                .build();

        service.addOrganizeMember(command);

        ArgumentCaptor<OrganizeMember> memberCaptor = ArgumentCaptor.forClass(OrganizeMember.class);
        verify(organizeMemberPort).save(memberCaptor.capture());
        assertEquals(OrganizeMemberRole.OWNER, memberCaptor.getValue().getRole());
    }

    @Test
    void addOrganizeMember_usesMemberRoleWhenRoleIsMissing() {
        when(organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(1L), UserId.of(2L))).thenReturn(false);

        OrganizeMemberAddCommand command = OrganizeMemberAddCommand.builder()
                .organizeId(1L)
                .userId(2L)
                .role(null)
                .build();

        service.addOrganizeMember(command);

        ArgumentCaptor<OrganizeMember> memberCaptor = ArgumentCaptor.forClass(OrganizeMember.class);
        verify(organizeMemberPort).save(memberCaptor.capture());
        assertEquals(OrganizeMemberRole.MEMBER, memberCaptor.getValue().getRole());
    }

    @Test
    void addOrganizeMember_doesNothingWhenAlreadyExists() {
        when(organizeMemberPort.existsByOrganizeAndUser(OrganizeId.of(1L), UserId.of(2L))).thenReturn(true);

        OrganizeMemberAddCommand command = OrganizeMemberAddCommand.builder()
                .organizeId(1L)
                .userId(2L)
                .role(OrganizeMemberRole.MEMBER)
                .build();

        service.addOrganizeMember(command);

        verify(organizeMemberPort, never()).save(any());
    }

    @Test
    void addOrganizeMember_throwsWhenCommandInvalid() {
        assertThrows(IllegalArgumentException.class, () -> service.addOrganizeMember(null));
        assertThrows(IllegalArgumentException.class, () -> service.addOrganizeMember(
                OrganizeMemberAddCommand.builder().organizeId(1L).build()
        ));
    }

    @Test
    void removeOrganizeMember_deletesByOrganizeAndUser() {
        service.removeOrganizeMember(1L, 2L);

        verify(organizeMemberPort).deleteByOrganizeAndUser(OrganizeId.of(1L), UserId.of(2L));
    }

    @Test
    void removeOrganizeMember_throwsWhenInputMissing() {
        assertThrows(IllegalArgumentException.class, () -> service.removeOrganizeMember(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.removeOrganizeMember(1L, null));
    }

    @Test
    void getOrganizeMembers_mapsDomainToSummary() {
        LocalDateTime joinedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        OrganizeMember member = OrganizeMember.create(OrganizeId.of(1L), UserId.of(2L), OrganizeMemberRole.OWNER, joinedAt);
        when(organizeMemberPort.findAllByOrganize(OrganizeId.of(1L))).thenReturn(List.of(member));

        List<OrganizeMemberSummary> result = service.getOrganizeMembers(1L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getUserId());
        assertEquals(OrganizeMemberRole.OWNER, result.get(0).getRole());
        assertEquals(joinedAt, result.get(0).getJoinedAt());
    }

    @Test
    void getOrganizeMembers_throwsWhenOrganizeIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> service.getOrganizeMembers(null));
    }
}
