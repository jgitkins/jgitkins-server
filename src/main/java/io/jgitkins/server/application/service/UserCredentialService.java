package io.jgitkins.server.application.service;

import io.jgitkins.server.application.dto.command.UserCredentialIssueCommand;
import io.jgitkins.server.application.dto.result.UserCredentialIssueResult;
import io.jgitkins.server.application.dto.result.UserCredentialSummary;
import io.jgitkins.server.application.port.in.UserCredentialIssueUseCase;
import io.jgitkins.server.application.port.in.UserCredentialQueryUseCase;
import io.jgitkins.server.application.port.in.UserCredentialRevokeUseCase;
import io.jgitkins.server.application.port.out.UserCredentialPort;
import io.jgitkins.server.domain.model.UserCredential;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCredentialService implements UserCredentialIssueUseCase,
                                               UserCredentialQueryUseCase,
                                               UserCredentialRevokeUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final UserCredentialPort userCredentialPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserCredentialIssueResult issueToken(UserCredentialIssueCommand command) {
        String token = generateToken();
        String hash = passwordEncoder.encode(token);
        UserCredential credential = UserCredential.issuePat(
                command.getUserId(),
                command.getName(),
                command.getDescription(),
                hash
        );
        UserCredential saved = userCredentialPort.save(credential);
        return new UserCredentialIssueResult(saved.getId(), token);
    }

    @Override
    public List<UserCredentialSummary> getPatList(Long userId) {
        return userCredentialPort.findAllByUserIdAndProvider(userId, "PAT")
                .stream()
                .map(credential -> new UserCredentialSummary(
                        credential.getId(),
                        credential.getProvider(),
                        credential.getName(),
                        credential.getDescription(),
                        credential.getCreatedAt(),
                        credential.getUpdatedAt()
                ))
                .toList();
    }

    @Override
    public void revokePat(Long userId, Long credentialId) {
        userCredentialPort.deleteByIdAndUserId(credentialId, userId);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return "jkpat_" + encoded;
    }
}
