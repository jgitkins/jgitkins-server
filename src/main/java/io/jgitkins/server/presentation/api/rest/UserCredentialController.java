package io.jgitkins.server.presentation.api.rest;

import io.jgitkins.server.application.dto.command.UserCredentialIssueCommand;
import io.jgitkins.server.application.dto.result.UserCredentialIssueResult;
import io.jgitkins.server.application.dto.result.UserCredentialSummary;
import io.jgitkins.server.application.port.in.UserCredentialIssueUseCase;
import io.jgitkins.server.application.port.in.UserCredentialQueryUseCase;
import io.jgitkins.server.application.port.in.UserCredentialRevokeUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "User Credentials")
@RequestMapping("/api/auth")
public class UserCredentialController {

    private final UserCredentialIssueUseCase userCredentialIssueUseCase;
    private final UserCredentialQueryUseCase userCredentialQueryUseCase;
    private final UserCredentialRevokeUseCase userCredentialRevokeUseCase;

    @Operation(summary = "Issue personal access token")
    @PostMapping("/pats")
    public ResponseEntity<ApiResponse<UserCredentialIssueResult>> issuePat(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        UserCredentialIssueCommand command = new UserCredentialIssueCommand(userId);
        UserCredentialIssueResult result = userCredentialIssueUseCase.issueToken(command);
        return ResponseFactory.created(result.getCredentialId(), result);
    }

    @Operation(summary = "List personal access tokens")
    @GetMapping("/pats")
    public ResponseEntity<ApiResponse<java.util.List<UserCredentialSummary>>> getPats(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(userCredentialQueryUseCase.getPatList(userId)));
    }

    @Operation(summary = "Revoke personal access token")
    @DeleteMapping("/pats/{credentialId}")
    public ResponseEntity<ApiResponse<Void>> revokePat(Authentication authentication,
                                                       @PathVariable Long credentialId) {
        Long userId = Long.valueOf(authentication.getName());
        userCredentialRevokeUseCase.revokePat(userId, credentialId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
