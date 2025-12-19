package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.AddOrganizeMemberCommand;

public interface OrganizeMemberAddUseCase {

    void addOrganizeMember(AddOrganizeMemberCommand command);
}
