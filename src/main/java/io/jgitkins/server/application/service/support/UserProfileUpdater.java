package io.jgitkins.server.application.service.support;

import io.jgitkins.server.domain.model.User;
import io.jgitkins.server.domain.model.UserIdentity;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class UserProfileUpdater {

	public User applyUserUpdates(User user,
								 String email,
								 String name,
								 String avatarUrl,
								 LocalDateTime loginAt) {
		User updated = maybeUpdateUser(user, email, name, avatarUrl);
		return updated.touchLogin(loginAt);
	}

	public UserIdentity updateIdentityIfChanged(UserIdentity identity,
												String email,
												boolean emailVerified,
												String name,
												String avatarUrl) {
		boolean changed = !equals(identity.getEmail(), normalize(email))
				|| identity.isEmailVerified() != emailVerified
				|| !equals(identity.getName(), normalize(name))
				|| !equals(identity.getAvatarUrl(), normalize(avatarUrl));
		return changed ? identity.updateProfile(email, emailVerified, name, avatarUrl) : identity;
	}

	private User maybeUpdateUser(User user, String email, String name, String avatarUrl) {
		boolean changed = !equals(user.getEmail(), normalize(email))
				|| !equals(user.getDisplayName(), normalize(name))
				|| !equals(user.getAvatarUrl(), normalize(avatarUrl));
		return changed ? user.updateProfile(email, name, avatarUrl) : user;
	}

	private boolean equals(String left, String right) {
		if (left == null) {
			return right == null;
		}
		return left.equals(right);
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
