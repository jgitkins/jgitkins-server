package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.command.OrganizeMemberAddCommand;
import io.jgitkins.server.application.dto.result.OrganizeMemberSummary;
import io.jgitkins.server.application.port.in.OrganizeMemberAddUseCase;
import io.jgitkins.server.application.port.in.OrganizeMemberQueryUseCase;
import io.jgitkins.server.application.port.in.OrganizeMemberRemoveUseCase;
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
public class OrganizeMemberService implements OrganizeMemberAddUseCase,
                                               OrganizeMemberRemoveUseCase,
                                               OrganizeMemberQueryUseCase {

    private final OrganizeMemberPersistencePort organizeMemberPersistencePort;

    @Override
    @Transactional
    public void addOrganizeMember(OrganizeMemberAddCommand command) {
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

    @Override
    @Transactional
    public void removeOrganizeMember(Long organizeId, Long userId) {
        if (organizeId == null || userId == null) {
            throw new IllegalArgumentException("OrganizeId and UserId are required to remove an organize member");
        }
        organizeMemberPersistencePort.deleteByOrganizeAndUser(OrganizeId.of(organizeId), UserId.of(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<OrganizeMemberSummary> getOrganizeMembers(Long organizeId) {
        if (organizeId == null) {
            throw new IllegalArgumentException("OrganizeId is required to load organize members");
        }
        return organizeMemberPersistencePort.findAllByOrganize(OrganizeId.of(organizeId))
                .stream()
                .map(member -> new OrganizeMemberSummary(
                        member.getUserId().getValue(),
                        member.getRole(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    private void validateCommand(OrganizeMemberAddCommand command) {
        if (command == null || command.getOrganizeId() == null || command.getUserId() == null) {
            throw new IllegalArgumentException("OrganizeId and UserId are required to add an organize member");
        }
    }
}
