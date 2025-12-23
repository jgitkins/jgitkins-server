package io.jgitkins.server.application.port.in;

public interface UserCredentialRevokeUseCase {
    void revokePat(Long userId, Long credentialId);
}
