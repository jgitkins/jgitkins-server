package io.jgitkins.server.presentation.api;

import io.jgitkins.server.application.dto.CreateOrganizeCommand;
import io.jgitkins.server.application.dto.OrganizeResult;
import io.jgitkins.server.application.dto.UpdateOrganizeCommand;
import io.jgitkins.server.application.port.in.*;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.jgitkins.server.presentation.dto.CreateOrganizeRequest;
import io.jgitkins.server.presentation.dto.UpdateOrganizeRequest;
import io.jgitkins.server.presentation.mapper.OrganizeRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Organize Management")
@RequestMapping("/api/organizes")
public class OrganizeController {

    private final CreateOrganizeUseCase createOrganizeUseCase;
    private final LoadOrganizeUseCase loadOrganizeUseCase;
    private final ListOrganizeUseCase listOrganizeUseCase;
    private final UpdateOrganizeUseCase updateOrganizeUseCase;
    private final DeleteOrganizeUseCase deleteOrganizeUseCase;
    private final OrganizeRequestMapper requestMapper;

    @Operation(summary = "Create Organize")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizeResult>> createOrganize(@RequestBody CreateOrganizeRequest request) {
        CreateOrganizeCommand command = requestMapper.toCommand(request);
        OrganizeResult result = createOrganizeUseCase.createOrganize(command);
        return ResponseFactory.created(result.getId(), result);
    }

    @Operation(summary = "List Organizes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizeResult>>> getOrganizes() {
        return ResponseEntity.ok(ApiResponse.success(listOrganizeUseCase.getOrganizes()));
    }

    @Operation(summary = "Get Organize")
    @GetMapping("/{organizeId}")
    public ResponseEntity<ApiResponse<OrganizeResult>> getOrganize(@PathVariable Long organizeId) {
        return ResponseEntity.ok(ApiResponse.success(loadOrganizeUseCase.getOrganize(organizeId)));
    }

    @Operation(summary = "Update Organize")
    @PutMapping("/{organizeId}")
    public ResponseEntity<ApiResponse<OrganizeResult>> updateOrganize(@PathVariable Long organizeId,
                                                                      @RequestBody UpdateOrganizeRequest request) {
        UpdateOrganizeCommand command = requestMapper.toCommand(request);
        return ResponseEntity.ok(ApiResponse.success(updateOrganizeUseCase.updateOrganize(organizeId, command)));
    }

    @Operation(summary = "Delete Organize")
    @DeleteMapping("/{organizeId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganize(@PathVariable Long organizeId) {
        deleteOrganizeUseCase.deleteOrganize(organizeId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
