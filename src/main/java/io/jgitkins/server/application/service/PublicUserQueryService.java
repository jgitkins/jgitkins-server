package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.result.UserSummary;
import io.jgitkins.server.application.port.in.PublicUserQueryUseCase;
import io.jgitkins.server.application.port.out.UserPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicUserQueryService implements PublicUserQueryUseCase {

    private final UserPort userPort;

    @Override
    public List<UserSummary> getUsers() {
        return userPort.findAll()
                .stream()
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getAvatarUrl(),
                        user.getCreatedAt()
                ))
                .toList();
    }
}
