package io.jgitkins.server.presentation.api;

import io.jgitkins.server.application.dto.RunnerDetailResult;
import io.jgitkins.server.application.dto.RunnerRegisterCommand;
import io.jgitkins.server.application.dto.RunnerRegistrationResult;
import io.jgitkins.server.application.port.in.RunnerActivateUseCase;
import io.jgitkins.server.application.port.in.RunnerDeleteUseCase;
import io.jgitkins.server.application.port.in.RunnerQueryUseCase;
import io.jgitkins.server.application.port.in.RunnerRegisterUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.jgitkins.server.presentation.dto.RunnerActivationRequest;
import io.jgitkins.server.presentation.dto.RunnerRegistrationRequest;
import io.jgitkins.server.presentation.dto.RunnerResponse;
import io.jgitkins.server.presentation.mapper.RunnerRegistrationMapper;
import io.jgitkins.server.presentation.mapper.RunnerResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Runner Management", description = "Runner registration and lifecycle APIs")
@RequestMapping({"/api/runners", "/api/runner"})
public class RunnerController {

    private final RunnerRegisterUseCase runnerRegisterUseCase;
    private final RunnerQueryUseCase runnerQueryUseCase;
    private final RunnerDeleteUseCase runnerDeleteUseCase;
    private final RunnerActivateUseCase runnerActivateUseCase;
    private final RunnerRegistrationMapper runnerRegistrationMapper;
    private final RunnerResponseMapper runnerResponseMapper;

    @Operation(summary = "Register Runner", description = "Register a runner instance and receive an authentication token")
    @PostMapping
    public ResponseEntity<ApiResponse<RunnerRegistrationResult>> registerRunner(@Valid @RequestBody RunnerRegistrationRequest request) {
        RunnerRegisterCommand registerCommand = runnerRegistrationMapper.toCommand(request);
        RunnerRegistrationResult result = runnerRegisterUseCase.register(registerCommand);
        return ResponseFactory.created(result.getRunnerId(), result);
    }

    @Operation(summary = "List Runners", description = "Retrieve all registered runners")
    @GetMapping
    public ResponseEntity<List<RunnerResponse>> getRunners() {
        List<RunnerDetailResult> results = runnerQueryUseCase.getRunners();
        return ResponseEntity.ok(runnerResponseMapper.toResponses(results));
    }

    @Operation(summary = "Get Runner", description = "Retrieve a runner detail by id")
    @GetMapping("/{runnerId}")
    public ResponseEntity<RunnerResponse> getRunner(@PathVariable Long runnerId) {
        RunnerDetailResult result = runnerQueryUseCase.getRunner(runnerId);
        return ResponseEntity.ok(runnerResponseMapper.toResponse(result));
    }

    @Operation(summary = "Delete Runner", description = "Delete a runner by id")
    @DeleteMapping("/{runnerId}")
    public ResponseEntity<Void> deleteRunner(@PathVariable Long runnerId) {
        runnerDeleteUseCase.deleteRunner(runnerId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate Runner", description = "Activate a runner and set it ONLINE")
//    @PostMapping("/{runnerId}/activate")
    @PostMapping("/activate")
    public ResponseEntity<RunnerResponse> activateRunner(
//            @PathVariable Long runnerId,
                                                         @Valid @RequestBody RunnerActivationRequest request,
                                                         HttpServletRequest httpServletRequest) {
        String clientIp = extractClientIp(httpServletRequest);
//        RunnerDetailResult result = runnerActivateUseCase.activate(runnerId, request.getToken(), clientIp);
        RunnerDetailResult result = runnerActivateUseCase.activate(request.getToken(), clientIp);
        return ResponseEntity.ok(runnerResponseMapper.toResponse(result));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
