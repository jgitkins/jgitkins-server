package io.jgitkins.server.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import io.jgitkins.server.application.port.out.LoadUserPort;
import org.springframework.stereotype.Component;

import java.util.Optional;


// TODO: Have to Customization
@RequiredArgsConstructor
@Component
public class UserPersistenceAdapter implements LoadUserPort {

    @Override
    public Optional<Object> getUser(String username) {
        return Optional.empty();
    }
}
