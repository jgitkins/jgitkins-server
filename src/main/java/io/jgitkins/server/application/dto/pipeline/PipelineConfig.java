package io.jgitkins.server.application.dto.pipeline;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PipelineConfig {

    private final List<PipelineRule> rules;

    public PipelineRule findRule(String branchName) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        for (PipelineRule rule : rules) {
            if (rule.matches(branchName)) {
                return rule;
            }
        }

        return null;
    }
}
