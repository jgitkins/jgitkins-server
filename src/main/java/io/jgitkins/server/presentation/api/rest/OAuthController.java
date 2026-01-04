package io.jgitkins.server.presentation.api.rest;

import io.jgitkins.server.application.dto.result.OAuthLoginResult;
import io.jgitkins.server.application.service.OAuthLoginService;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.dto.OAuthLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "OAuth")
@RequestMapping("/api/auth/oauth")
public class OAuthController {

    private final OAuthLoginService oauthLoginService;

    @Operation(summary = "Issue JWT token from OAuth login data")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OAuthLoginResult>> login(@RequestBody OAuthLoginRequest request) {
        OAuthLoginResult result = oauthLoginService.loginWithOidcAttributes(
                request.provider(),
                request.subject(),
                request.email(),
                request.name(),
                request.emailVerified(),
                request.avatarUrl()
        );
        return ApiResponse.ok(result);
    }
}
