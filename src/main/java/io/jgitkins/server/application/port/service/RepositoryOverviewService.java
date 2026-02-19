package io.jgitkins.server.application.port.service;

import io.jgitkins.server.application.dto.FileEntry;
import io.jgitkins.server.application.dto.RepositoryKey;
import io.jgitkins.server.application.dto.result.BranchSearchResult;
import io.jgitkins.server.application.dto.result.RepositoryOverviewResult;
import io.jgitkins.server.application.dto.result.RepositoryResult;
import io.jgitkins.server.application.port.in.BranchLoadUseCase;
import io.jgitkins.server.application.port.in.FileTreeLoadUseCase;
import io.jgitkins.server.application.port.in.RepositoryLoadUseCase;
import io.jgitkins.server.application.port.in.RepositoryOverviewUseCase;
import io.jgitkins.server.application.port.out.CurrentUserPort;
import io.jgitkins.server.application.service.GitRepositoryAccessService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RepositoryOverviewService implements RepositoryOverviewUseCase {

	private static final String ROOT_PATH = "";

	private final RepositoryLoadUseCase repositoryLoadUseCase;
	private final BranchLoadUseCase branchLoadUseCase;
	private final FileTreeLoadUseCase fileTreeLoadUseCase;
	private final CurrentUserPort currentUserPort;
	private final GitRepositoryAccessService gitRepositoryAccessService;

	@Override
	public RepositoryOverviewResult getOverview(Long repositoryId, String branch) throws IOException {

		RepositoryResult repository = repositoryLoadUseCase.getRepository(repositoryId);
		RepositoryKey key = resolveRepositoryKey(repository);
		List<BranchSearchResult> branches = branchLoadUseCase.getBranches(repositoryId);
		String selectedBranch = resolveBranch(branch, branches);
		List<FileEntry> tree = loadTree(key, selectedBranch);
		Long userId = currentUserPort.currentUserId().orElse(null);
		GitRepositoryAccessService.RepositoryPermission permission =
				gitRepositoryAccessService.resolvePermission(null, key != null ? key.namespace() : null, key != null ? key.repoName() : null, userId);

		return RepositoryOverviewResult.builder()
				.repository(repository)
				.branches(branches)
				.tree(tree)
				.selectedBranch(selectedBranch)
				.role(permission.role())
				.writable(permission.writable())
				.build();
	}

	private List<FileEntry> loadTree(RepositoryKey key, String selectedBranch) throws IOException {
		if (key == null || !StringUtils.hasText(selectedBranch)) {
			return List.of();
		}
		return fileTreeLoadUseCase.getTree(key.namespace(), key.repoName(), selectedBranch, ROOT_PATH);
	}

	private String resolveBranch(String branch, List<BranchSearchResult> branches) {
		if (StringUtils.hasText(branch)) {
			return branch;
		}

		return branches.stream()
				.filter(b -> b.isDefaultBranch())
				.findFirst()
				.map(BranchSearchResult::getName)
				.orElseGet(() -> branches.isEmpty() ? null : branches.get(0).getName());
	}

	private RepositoryKey resolveRepositoryKey(RepositoryResult repository) {
		if (repository == null) {
			return null;
		}
		RepositoryKey key = RepositoryKey.fromPath(repository.getClonePath());
		return key != null ? key : RepositoryKey.fromPath(repository.getPath());
	}
}
