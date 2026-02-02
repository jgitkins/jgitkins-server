package io.jgitkins.server.application.service;

import io.jgitkins.server.application.port.out.UserIdentityPort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.application.service.support.UserProfileUpdater;
import io.jgitkins.server.application.service.support.UsernameAllocator;
import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserIdentity;
import io.jgitkins.server.domain.model.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserPort userPort;
    private final UserIdentityPort userIdentityPort;
    private final UsernameAllocator usernameAllocator;
    private final UserProfileUpdater userProfileUpdater;

    public User loginOrSignUp(String providerName,
                              String providerSub,
                              String email,
                              boolean emailVerified,
                              String name,
                              String avatarUrl) {

        LocalDateTime loginAt = LocalDateTime.now();

        if (!isProviderIdentityReady(providerName, providerSub)) {
            throw new IllegalArgumentException("Provider identity is required");
        }

        return userIdentityPort.findByProvider(providerName, providerSub)
                .map(identity -> signin(identity, email, emailVerified, name, avatarUrl, loginAt))
                .orElseGet(() -> signinWithSignUp(providerName, providerSub, email, emailVerified, name, avatarUrl, loginAt));
    }

    private User signin(UserIdentity identity,
                        String email,
                        boolean emailVerified,
                        String name,
                        String avatarUrl,
                        LocalDateTime loginAt) {

        User user = userPort.findById(identity.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for identity"));

        User persistedUser = persistUserWithUpdates(user, email, name, avatarUrl, loginAt); // 데이터가 변경된 부분이 있다면, 업데이트를 진행
        UserIdentity updatedIdentity = userProfileUpdater.updateIdentityIfChanged(identity, email, emailVerified, name, avatarUrl);
        if (updatedIdentity != identity) {
            userIdentityPort.save(updatedIdentity);
        }

        return persistedUser;
    }

    private User signinWithSignUp(String providerName,
                                   String providerSub,
                                   String email,
                                   boolean emailVerified,
                                   String name,
                                   String avatarUrl,
                                   LocalDateTime loginAt) {

        User user = findOrCreateUserForIdentity(email, name, avatarUrl, providerName, providerSub);
        User persisted = persistUserWithUpdates(user, email, name, avatarUrl, loginAt);

        UserIdentity identity = UserIdentity.create(
                persisted.getId(),
                providerName,
                providerSub,
                email,
                emailVerified,
                name,
                avatarUrl
        );
        userIdentityPort.save(identity);

        return persisted;
    }

    private User findOrCreateUserForIdentity(String email,
                                           String name,
                                           String avatarUrl,
                                           String providerName,
                                           String providerSub) {
        return findExistingUserByEmail(email)
                .orElseGet(() -> createPendingUser(email, name, avatarUrl, providerName, providerSub));
    }

    private Optional<User> findExistingUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userPort.findByEmail(email.trim());
    }

    private boolean isProviderIdentityReady(String providerName, String providerSub) {
        return providerName != null && !providerName.isBlank()
                && providerSub != null && !providerSub.isBlank();
    }

    private User createPendingUser(String email,
                                   String name,
                                   String avatarUrl,
                                   String providerName,
                                   String providerSub) {

        String baseUsername = usernameAllocator.deriveBaseUsername(email, providerName, providerSub);
        String username = usernameAllocator.allocateUniqueUsername(baseUsername, providerSub);
        return User.createWithStatus(username, email, name, avatarUrl, UserStatus.PENDING);
    }

    private User persistUserWithUpdates(User user,
                                        String email,
                                        String name,
                                        String avatarUrl,
                                        LocalDateTime loginAt) {
        User updatedUser = userProfileUpdater.applyUserUpdates(user, email, name, avatarUrl, loginAt);
        return userPort.save(updatedUser);
    }

}
