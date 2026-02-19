package io.jgitkins.server.presentation.api.web;

import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.in.RepositoryOverviewUseCase;
import io.jgitkins.server.presentation.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Web Repository")
@RequestMapping("/api/internal/repositories")
public class WebRepositoryController {

	private final RepositoryLoadUseCase repositoryLoadUseCase;
	private final RepositoryOverviewUseCase repositoryOverviewUseCase;

	@Operation(summary = "Get User Repositories by Username (Web)")
	@GetMapping("/users/{username}")
	public ResponseEntity<ApiResponse<List<RepositoryResult>>> getUserRepositories(@PathVariable("username") String username) {
		return ApiResponse.ok(repositoryLoadUseCase.getRepositoriesByUsername(username));
	}

	@Operation(summary = "Get Repository Overview by Namespace/Repo (Web)")
	@GetMapping("/{namespace}/{repoName}/overview")
	public ResponseEntity<ApiResponse<io.jgitkins.server.application.dto.result.RepositoryOverviewResult>> getRepositoryOverviewByPath(
			@PathVariable String namespace,
			@PathVariable String repoName,
			@org.springframework.web.bind.annotation.RequestParam(name = "branch", required = false) String branch
	) throws java.io.IOException {
		RepositoryResult repository = repositoryLoadUseCase.getRepositoryByPath(namespace, repoName);
		return ApiResponse.ok(repositoryOverviewUseCase.getOverview(repository.getId(), branch));
	}
}
