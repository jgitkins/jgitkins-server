package io.jgitkins.server.presentation.api;

import io.jgitkins.server.application.dto.OrganizeCreationCommand;
import io.jgitkins.server.application.dto.OrganizeCreationResult;
import io.jgitkins.server.application.port.in.OrganizeCreationUseCase;
import io.jgitkins.server.application.port.in.OrganizeDeletionUseCase;
import io.jgitkins.server.application.port.in.OrganizeLoadUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.jgitkins.server.presentation.common.ResponseFactory;
import io.jgitkins.server.presentation.dto.OrganizeCreationRequest;
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

    private final OrganizeCreationUseCase organizeCreationUseCase;
    private final OrganizeLoadUseCase organizeLoadUseCase;
    private final OrganizeDeletionUseCase organizeDeletionUseCase;
//    private final OrganizeUpdateUseCase organizeUpdateUseCase;

    private final OrganizeRequestMapper organizeRequestMapper;

    @Operation(summary = "Create Organize")
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizeCreationResult>> createOrganize(@RequestBody OrganizeCreationRequest request) {
        OrganizeCreationCommand command = organizeRequestMapper.toCommand(request);
        OrganizeCreationResult result = organizeCreationUseCase.createOrganize(command);
        return ResponseFactory.created(result.getId(), result);
    }

    @Operation(summary = "List Organizes")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizeCreationResult>>> getOrganizes() {
        return ResponseEntity.ok(ApiResponse.success(organizeLoadUseCase.getOrganizes()));
    }

    @Operation(summary = "Get Organize")
    @GetMapping("/{organizeId}")
    public ResponseEntity<ApiResponse<OrganizeCreationResult>> getOrganize(@PathVariable Long organizeId) {
        return ResponseEntity.ok(ApiResponse.success(organizeLoadUseCase.getOrganize(organizeId)));
    }

//    @Operation(summary = "Update Organize")
//    @PutMapping("/{organizeId}")
//    public ResponseEntity<ApiResponse<OrganizeCreationResult>> updateOrganize(@PathVariable Long organizeId,
//                                                                              @RequestBody UpdateOrganizeRequest request) {
//        UpdateOrganizeCommand command = requestMapper.toCommand(request);
//        return ResponseEntity.ok(ApiResponse.success(organizeUpdateUseCase.updateOrganize(organizeId, command)));
//    }

    @Operation(summary = "Delete Organize")
    @DeleteMapping("/{organizeId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrganize(@PathVariable Long organizeId) {
        organizeDeletionUseCase.deleteOrganize(organizeId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
