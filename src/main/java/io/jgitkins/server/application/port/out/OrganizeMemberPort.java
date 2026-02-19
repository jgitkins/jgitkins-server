package io.jgitkins.server.application.port.out;

import io.jgitkins.server.domain.model.OrganizeMember;
import io.jgitkins.server.domain.model.vo.OrganizeId;
import io.jgitkins.server.domain.model.vo.UserId;
import java.util.Optional;

public interface OrganizeMemberPort {

    OrganizeMember save(OrganizeMember member);

    boolean existsByOrganizeAndUser(OrganizeId organizeId, UserId userId);

    Optional<OrganizeMember> findByOrganizeAndUser(OrganizeId organizeId, UserId userId);

    void deleteByOrganizeAndUser(OrganizeId organizeId, UserId userId);

    java.util.List<OrganizeMember> findAllByOrganize(OrganizeId organizeId);
}
