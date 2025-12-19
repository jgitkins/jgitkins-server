package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.exception.ConflictException;
import io.jgitkins.server.application.common.exception.ResourceNotFoundException;
import io.jgitkins.server.application.dto.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.OrganizeCreationResult;
import io.jgitkins.server.application.mapper.OrganizeApplicationMapper;
import io.jgitkins.server.application.port.in.OrganizeCreationUseCase;
import io.jgitkins.server.application.port.in.OrganizeDeletionUseCase;
import io.jgitkins.server.application.port.in.OrganizeLoadUseCase;
import io.jgitkins.server.application.port.out.OrganizePersistencePort;
import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizeService implements OrganizeCreationUseCase,
                                        OrganizeLoadUseCase,
//                                                       OrganizeUpdateUseCase,
                                        OrganizeDeletionUseCase {

    private final OrganizePersistencePort organizePersistencePort;
    private final OrganizeApplicationMapper organizeApplicationMapper;

    @Override
    @Transactional
    public OrganizeCreationResult createOrganize(OrganizeCreationCommand command) {

        OrganizeName organizeName = OrganizeName.from(command.getName());

        organizePersistencePort.findByName(organizeName)
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS, "Organize name already exists: " + organizeName.getValue());
                });

        Organize organize = Organize.create(command.getName(),
                                            command.getOwnerId(),
                                            command.getDescription());

        Organize saved = organizePersistencePort.save(organize);

        return organizeApplicationMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizeCreationResult getOrganize(Long organizeId) {
        Organize organize = organizePersistencePort.findById(OrganizeId.of(organizeId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
                        "Organize not found: " + organizeId));
        return organizeApplicationMapper.toDto(organize);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizeCreationResult> getOrganizes() {
        return organizePersistencePort.findAll()
                .stream()
                .map(organizeApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

//    @Override
//    @Transactional
//    public OrganizeCreationResult updateOrganize(Long organizeId, UpdateOrganizeCommand command) {
//        Organize organize = organizePersistencePort.findById(OrganizeId.of(organizeId))
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORGANIZE_NOT_FOUND,
//                        "Organize not found: " + organizeId));
//
//        if (command.getName() != null) {
//            OrganizeName newName = OrganizeName.from(command.getName());
//            boolean changed = !organize.getName().equals(newName);
//            if (changed) {
//                organizePersistencePort.findByName(newName)
//                        .ifPresent(existing -> {
//                            throw new ConflictException(ErrorCode.ORGANIZE_ALREADY_EXISTS,
//                                    "Organize name already exists: " + newName.getValue());
//                        });
//            }
//        }
//
//        Organize updated = organize.updateMetadata(
//                command.getName(),
//                command.getOwnerId(),
//                command.getDescription()
//        );
//        Organize persisted = organizePersistencePort.update(updated);
//        return organizeApplicationMapper.toDto(persisted);
//    }

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
