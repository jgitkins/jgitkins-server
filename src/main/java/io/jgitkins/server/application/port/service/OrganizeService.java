package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.CreateOrganizeCommand;
import io.jgitkins.server.application.dto.OrganizeResult;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;
import io.jgitkins.server.application.mapper.OrganizeApplicationMapper;
import io.jgitkins.server.application.port.in.CreateOrganizeUseCase;
import io.jgitkins.server.application.port.in.DeleteOrganizeUseCase;
import io.jgitkins.server.application.port.in.ListOrganizeUseCase;
import io.jgitkins.server.application.port.in.LoadOrganizeUseCase;
import io.jgitkins.server.application.port.in.UpdateOrganizeUseCase;
import io.jgitkins.server.application.port.out.OrganizePersistencePort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizePath;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizeService implements CreateOrganizeUseCase,
                                         LoadOrganizeUseCase,
                                         ListOrganizeUseCase,
                                         UpdateOrganizeUseCase,
                                         DeleteOrganizeUseCase {

    private final OrganizePersistencePort organizePersistencePort;
    private final OrganizeApplicationMapper organizeApplicationMapper;

    @Override
    @Transactional
    public OrganizeResult createOrganize(CreateOrganizeCommand command) {

        OrganizePath path = OrganizePath.from(command.getPath());

        organizePersistencePort.findByPath(path)
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS, "Organize path already exists: " + path.getValue());
                });

        Organize organize = Organize.create(command.getName(),
                                            command.getPath(),
                                            command.getOwnerId(),
                                            command.getDescription());

        Organize saved = organizePersistencePort.save(organize);

        return organizeApplicationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizeResult getOrganize(Long organizeId) {
        Organize organize = organizePersistencePort.findById(OrganizeId.of(organizeId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
        return organizeApplicationMapper.toDto(organize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizeResult> getOrganizes() {
        return organizePersistencePort.findAll()
                .stream()
                .map(organizeApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganizeResult updateOrganize(Long organizeId, UpdateOrganizeCommand command) {
        Organize organize = organizePersistencePort.findById(OrganizeId.of(organizeId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));

        if (command.getPath() != null) {
            OrganizePath newPath = OrganizePath.from(command.getPath());
            boolean pathChanged = !organize.getPath().getValue().equals(newPath.getValue());
            if (pathChanged) {
                organizePersistencePort.findByPath(newPath)
                        .ifPresent(existing -> {
                            throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS,
                                    "Organize path already exists: " + newPath.getValue());
                        });
            }
        }

        Organize updated = organize.updateMetadata(
                command.getName(),
                command.getPath(),
                command.getOwnerId(),
                command.getDescription()
        );
        Organize persisted = organizePersistencePort.update(updated);
        return organizeApplicationMapper.toDto(persisted);
    }

    @Override
    @Transactional
    public void deleteOrganize(Long organizeId) {
        OrganizeId id = OrganizeId.of(organizeId);
        organizePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
        organizePersistencePort.delete(id);
    }
}
