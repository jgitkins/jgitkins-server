package io.jgitkins.server.infrastructure.adapter.persistence;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.jgitkins.server.application.port.out.BranchPersistencePort;
import org.springframework.stereotype.Component;

@Component
public class InMemoryBranchPersistenceAdapter implements BranchPersistencePort {

    private final Map<String, Set<String>> branchStore = new ConcurrentHashMap<>();

    @Override
    public void saveBranch(String taskCd, String repoName, String branchName) {
        branchStore.computeIfAbsent(key(taskCd, repoName), k -> ConcurrentHashMap.newKeySet())
                .add(branchName);
    }

    @Override
    public void deleteBranch(String taskCd, String repoName, String branchName) {
        branchStore.computeIfPresent(key(taskCd, repoName), (k, v) -> {
            v.remove(branchName);
            return v.isEmpty() ? null : v;
        });
    }

    private String key(String taskCd, String repoName) {
        return taskCd + ":" + repoName;
    }
}
