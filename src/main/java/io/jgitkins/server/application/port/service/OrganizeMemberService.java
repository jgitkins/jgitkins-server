package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.command.OrganizeMemberAddCommand;
import io.jgitkins.server.application.dto.result.OrganizeMemberSummary;
import io.jgitkins.server.application.port.in.OrganizeMemberAddUseCase;
import io.jgitkins.server.application.port.in.OrganizeMemberQueryUseCase;
import io.jgitkins.server.application.port.in.OrganizeMemberRemoveUseCase;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeMemberRole;
import io.jgitkins.server.domain.model.vo.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizeMemberService implements OrganizeMemberAddUseCase,
                                               OrganizeMemberRemoveUseCase,
                                               OrganizeMemberQueryUseCase {

    private final OrganizeMemberPort organizeMemberPort;

    @Override
    @Transactional
    public void addOrganizeMember(OrganizeMemberAddCommand command) {
        OrganizeMemberAddCommand safeCommand = requireCommand(command);
        OrganizeId organizeId = OrganizeId.of(safeCommand.getOrganizeId());
        UserId userId = UserId.of(safeCommand.getUserId());
        if (organizeMemberPort.existsByOrganizeAndUser(organizeId, userId)) {
            return;
        }
        OrganizeMemberRole role = safeCommand.getRole() != null
                ? safeCommand.getRole()
                : OrganizeMemberRole.MEMBER;
        OrganizeMember member = OrganizeMember.create(organizeId, userId, role, null);
        organizeMemberPort.save(member);
    }

    @Override
    @Transactional
    public void removeOrganizeMember(Long organizeId, Long userId) {
        requireOrganizeAndUser(organizeId, userId,
                "OrganizeId and UserId are required to remove an organize member");
        organizeMemberPort.deleteByOrganizeAndUser(OrganizeId.of(organizeId), UserId.of(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizeMemberSummary> getOrganizeMembers(Long organizeId) {
        requireOrganizeId(organizeId, "OrganizeId is required to load organize members");
        return organizeMemberPort.findAllByOrganize(OrganizeId.of(organizeId))
                .stream()
                .map(member -> new OrganizeMemberSummary(
                        member.getUserId().getValue(),
                        member.getRole(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    private OrganizeMemberAddCommand requireCommand(OrganizeMemberAddCommand command) {
        if (command == null || command.getOrganizeId() == null || command.getUserId() == null) {
            throw new IllegalArgumentException("OrganizeId and UserId are required to add an organize member");
        }
        return command;
    }

    private void requireOrganizeId(Long organizeId, String message) {
        if (organizeId == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireOrganizeAndUser(Long organizeId, Long userId, String message) {
        if (organizeId == null || userId == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
