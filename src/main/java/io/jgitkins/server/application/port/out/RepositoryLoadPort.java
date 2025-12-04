package io.jgitkins.server.application.port.out;

import java.util.Optional;

public interface RepositoryLoadPort {

    /**
     * 주어진 taskCd(조직/프로젝트 코드)와 repository 이름으로 Repository의 PK를 조회
     *
     * @param taskCd   조직/프로젝트 식별 코드
     * @param repoName 저장소 이름
     * @return Repository PK (없으면 Optional.empty())
     */
    Optional<Long> findRepositoryId(String taskCd, String repoName);
}
