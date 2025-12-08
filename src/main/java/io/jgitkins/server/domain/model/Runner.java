package io.jgitkins.server.domain.model;

import io.jgitkins.server.domain.model.vo.RunnerStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Runner aggregate captures registration metadata so orchestration layers can
 * reason about availability without leaking persistence concerns.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Runner {

    private final Long id;
    private final String token;
    private final String description;
    private final RunnerStatus status;
    private final LocalDateTime lastHeartbeatAt;
    private final LocalDateTime createdAt;

    public static Runner create(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Runner description is required when registering");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Runner(null,
                          generateToken(),
                          description.trim(),
                          RunnerStatus.OFFLINE,
                          now,
                          now);
    }

    private static String generateToken() {
        return "RNR-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24).toUpperCase();
    }

    public Runner withId(Long id) {
        return new Runner(id,
                          token,
                          description,
                          status,
                          lastHeartbeatAt,
                          createdAt);
    }

    public Runner withStatus(RunnerStatus newStatus) {
        return new Runner(id,
                          token,
                          description,
                          newStatus,
                          lastHeartbeatAt,
                          createdAt);
    }

    public Runner withLastHeartbeatAt(LocalDateTime heartbeatAt) {
        return new Runner(id,
                          token,
                          description,
                          status,
                          heartbeatAt,
                          createdAt);
    }

    public Runner activate() {
        return new Runner(id,
                          token,
                          description,
                          RunnerStatus.ONLINE,
                          LocalDateTime.now(),
                          createdAt);
    }

    public static Runner restore(Long id,
                                 String token,
                                 String description,
                                 RunnerStatus status,
                                 LocalDateTime lastHeartbeatAt,
                                 LocalDateTime createdAt) {
        return new Runner(id,
                          token,
                          description,
                          status,
                          lastHeartbeatAt,
                          createdAt);
    }
}
