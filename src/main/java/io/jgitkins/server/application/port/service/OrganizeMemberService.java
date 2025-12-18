package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.AddOrganizeMemberCommand;
import io.jgitkins.server.application.port.in.AddOrganizeMemberUseCase;
import io.jgitkins.server.application.port.out.OrganizeMemberPersistencePort;
import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeMemberRole;
import io.jgitkins.server.domain.model.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizeMemberService implements AddOrganizeMemberUseCase {

    private final OrganizeMemberPersistencePort organizeMemberPersistencePort;

    @Override
    @Transactional
    public void addOrganizeMember(AddOrganizeMemberCommand command) {
        validateCommand(command);
        OrganizeId organizeId = OrganizeId.of(command.getOrganizeId());
        UserId userId = UserId.of(command.getUserId());
        if (organizeMemberPersistencePort.existsByOrganizeAndUser(organizeId, userId)) {
            return;
        }
        OrganizeMemberRole role = command.getRole() != null ? command.getRole() : OrganizeMemberRole.MEMBER;
        OrganizeMember member = OrganizeMember.create(organizeId, userId, role, null);
        organizeMemberPersistencePort.save(member);
    }

    private void validateCommand(AddOrganizeMemberCommand command) {
        if (command == null || command.getOrganizeId() == null || command.getUserId() == null) {
            throw new IllegalArgumentException("OrganizeId and UserId are required to add an organize member");
        }
    }
}
