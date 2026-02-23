package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.command.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.result.OrganizeCreationResult;
import io.jgitkins.server.application.mapper.OrganizeApplicationMapper;
import io.jgitkins.server.application.port.in.OrganizeCreationUseCase;
import io.jgitkins.server.application.port.in.OrganizeDeletionUseCase;
import io.jgitkins.server.application.port.in.OrganizeLoadUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.port.out.OrganizeMemberPort;
import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import io.jgitkins.server.domain.model.vo.UserId;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizeService implements OrganizeCreationUseCase,
                                        OrganizeLoadUseCase,
                                        OrganizeDeletionUseCase {

    private final OrganizePort organizePort;
    private final UserPort userPort;
    private final OrganizeMemberPort organizeMemberPort;
    private final CurrentUserPort currentUserPort;

    private final OrganizeApplicationMapper organizeApplicationMapper;

    @Override
    @Transactional
    public OrganizeCreationResult createOrganize(OrganizeCreationCommand command) {

        OrganizeName organizeName = OrganizeName.from(command.getName());

        assertOrganizeNameAvailable(organizeName);
        assertNamespaceAvailable(organizeName);

        Organize organize = Organize.create(command.getName(),
                                            command.getOwnerId(),
                                            command.getDescription());

        Organize saved = organizePort.save(organize);

        return organizeApplicationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizeCreationResult getOrganize(Long organizeId) {
        Organize organize = organizePort.findById(OrganizeId.of(organizeId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
        return organizeApplicationMapper.toDto(organize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizeCreationResult> getOrganizes() {
        return organizePort.findAll()
                .stream()
                .map(organizeApplicationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizeCreationResult> getAccessibleOrganizes() {
        Optional<Long> requesterId = currentUserPort.currentUserId();
        if (requesterId.isEmpty()) {
            return List.of();
        }
        UserId userId = UserId.of(requesterId.get());
        Map<OrganizeId, Boolean> membershipCache = new HashMap<>();
        return organizePort.findAll()
                .stream()
                .filter(organize -> isAccessible(organize, userId, membershipCache))
                .map(organizeApplicationMapper::toDto)
                .toList();
    }

    private boolean isAccessible(Organize organize,
                                 UserId userId,
                                 Map<OrganizeId, Boolean> membershipCache) {
        if (organize == null || organize.getId() == null) {
            return false;
        }
        if (organize.getOwnerId() != null && organize.getOwnerId().getValue().equals(userId.getValue())) {
            return true;
        }
        OrganizeId organizeId = organize.getId();
        return membershipCache.computeIfAbsent(organizeId,
                id -> organizeMemberPort.existsByOrganizeAndUser(id, userId));
    }

    private void assertOrganizeNameAvailable(OrganizeName organizeName) {
        organizePort.findByName(organizeName)
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS,
                            "Organize name already exists: " + organizeName.getValue());
                });
    }

    private void assertNamespaceAvailable(OrganizeName organizeName) {
        userPort.findByUsername(organizeName.getValue())
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS,
                            "Namespace already exists: " + organizeName.getValue());
                });
    }

    @Override
    @Transactional
    public void deleteOrganize(Long organizeId) {
        OrganizeId id = OrganizeId.of(organizeId);
        organizePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
        organizePort.delete(id);
    }
}
