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
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RepositoryOverviewService implements RepositoryOverviewUseCase {

	private final RepositoryLoadUseCase repositoryLoadUseCase;
	private final BranchLoadUseCase branchLoadUseCase;
	private final FileTreeLoadUseCase fileTreeLoadUseCase;

	@Override
	public RepositoryOverviewResult getOverview(Long repositoryId, String branch) throws IOException {

		// repository 기본 정보
		RepositoryResult repository = repositoryLoadUseCase.getRepository(repositoryId);

		RepositoryKey key = resolveRepositoryKey(repository);

		List<BranchSearchResult> branches = branchLoadUseCase.getBranches(repositoryId);

		String selectedBranch = resolveBranch(branch, branches);

		List<FileEntry> tree = key == null
				? List.of()
				: fileTreeLoadUseCase.getTree(key.namespace(), key.repoName(), selectedBranch, "");

		return RepositoryOverviewResult.builder()
				.repository(repository)
				.branches(branches)
				.tree(tree)
				.selectedBranch(selectedBranch)
				.build();
	}


	private String resolveBranch(String branch, List<BranchSearchResult> branches) {
		if (StringUtils.hasText(branch)) {
			return branch;
		}

		return branches.stream()
				.filter(b -> b.isDefaultBranch())
				.findFirst()
				.get()
				.getName();
//		return "main";
	}

	private RepositoryKey resolveRepositoryKey(RepositoryResult repository) {
		if (repository == null) {
			return null;
		}
		RepositoryKey key = RepositoryKey.fromPath(repository.getClonePath());
		if (key != null) {
			return key;
		}
		return RepositoryKey.fromPath(repository.getPath());
	}
}
