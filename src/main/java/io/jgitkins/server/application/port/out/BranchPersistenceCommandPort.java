package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.Branch;

public interface BranchPersistenceCommandPort {
    void create(Branch branch);
    void delete(Long id);
}
