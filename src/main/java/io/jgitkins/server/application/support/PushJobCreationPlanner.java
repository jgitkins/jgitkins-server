package io.jgitkins.server.application.support;

import static io.jgitkins.server.application.dto.result.PipelineSkipReason.SKIPPED_NO_RULE;
import static io.jgitkins.server.application.dto.result.PipelineSkipReason.SKIPPED_PIPELINE_NOT_FOUND;

import io.jgitkins.server.application.dto.pipeline.PipelineConfig;
import io.jgitkins.server.application.dto.pipeline.PipelineRule;
import io.jgitkins.server.application.dto.result.JobPlan;
import io.jgitkins.server.application.port.out.FileGitPort;
import io.jgitkins.server.application.port.out.PipelineConfigPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushJobCreationPlanner {

    private static final String PIPELINE_ROOT = ".jgitkins/";

    private final PipelineConfigPort configPort;
    private final FileGitPort fileGitPort;

    public JobPlan plan(String taskCd, String repoName, String branchName, String commitHash) {
        PipelineConfig config = configPort.read(taskCd, repoName, commitHash);
        PipelineRule rule = config == null ? null : config.findRule(branchName);
        if (rule == null) {
            return JobPlan.skip(SKIPPED_NO_RULE);
        }

        String pipelineFilePath = toPipelineFilePath(rule.getFile());
        if (!fileGitPort.exists(taskCd, repoName, commitHash, pipelineFilePath)) {
            return JobPlan.skip(SKIPPED_PIPELINE_NOT_FOUND);
        }

        return JobPlan.create(pipelineFilePath);
    }

    private String toPipelineFilePath(String file) {
        if (file.startsWith(PIPELINE_ROOT)) {
            return file;
        }
        return PIPELINE_ROOT + file;
    }
}
