package io.jgitkins.server.application.port.in;

import io.jgitkins.server.application.dto.AddOrganizeMemberCommand;

public interface AddOrganizeMemberUseCase {

    void addOrganizeMember(AddOrganizeMemberCommand command);
}
