package io.jgitkins.server.application.support;

import io.jgitkins.server.application.port.out.OrganizePort;
import io.jgitkins.server.application.port.out.UserPort;
import io.jgitkins.server.domain.model.vo.OrganizeName;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernameAllocator {

	private final UserPort userPort;
	private final OrganizePort organizePort;

	public String deriveBaseUsername(String email, String providerName, String providerSub) {
		if (email != null && !email.isBlank()) {
			String local = email.split("@")[0];
			return sanitize(local.toLowerCase());
		}
		String seed = providerName + "-" + providerSub;
		return sanitize(seed.toLowerCase());
	}

	public String allocateUniqueUsername(String baseUsername, String providerSub) {
		String providerSuffix = providerSub == null ? "user" : providerSub.substring(Math.max(0, providerSub.length() - 6));
		String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

		String[] candidates = new String[] {
				baseUsername,
				baseUsername + "-" + providerSuffix.toLowerCase(),
				baseUsername + "-" + randomSuffix.toLowerCase(),
				baseUsername + "-" + System.currentTimeMillis()
		};

		for (String candidate : candidates) {
			if (isNamespaceAvailable(candidate)) {
				return candidate;
			}
		}

		return baseUsername + "-" + System.nanoTime();
	}

	private String sanitize(String value) {
		return value.replaceAll("[^a-z0-9._-]", "-");
	}

	private boolean isNamespaceAvailable(String username) {
		if (userPort.findByUsername(username).isPresent()) {
			return false;
		}
		return findOrganizeByName(username).isEmpty();
	}

	private Optional<io.jgitkins.server.domain.aggregate.Organize> findOrganizeByName(String namespace) {
		try {
			return organizePort.findByName(OrganizeName.from(namespace));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
