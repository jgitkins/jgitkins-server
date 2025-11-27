package io.jgitkins.server.application.port.out;

import java.util.Optional;

public interface LoadUserPort {
    Optional<Object> getUser(String username);
}
