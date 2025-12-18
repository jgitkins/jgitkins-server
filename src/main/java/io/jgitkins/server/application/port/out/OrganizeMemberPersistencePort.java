package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.UserId;

public interface OrganizeMemberPersistencePort {

    OrganizeMember save(OrganizeMember member);

    boolean existsByOrganizeAndUser(OrganizeId organizeId, UserId userId);
}
