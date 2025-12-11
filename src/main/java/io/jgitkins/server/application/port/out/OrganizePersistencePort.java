package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.aggregate.Organize;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.OrganizePath;

import java.util.List;
import java.util.Optional;

public interface OrganizePersistencePort {
    Organize save(Organize organize);

    Organize update(Organize organize);

    Optional<Organize> findById(OrganizeId organizeId);

    Optional<Organize> findByPath(OrganizePath path);

    List<Organize> findAll();

    void delete(OrganizeId organizeId);
}
