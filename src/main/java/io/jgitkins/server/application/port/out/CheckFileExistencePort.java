package io.jgitkins.server.application.port.out;

public interface CheckFileExistencePort {
    /**
     * 특정 커밋 시점에 파일이 존재하는지 확인
     * 
     * @param repoName   저장소 이름
     * @param commitHash 커밋 해시
     * @param filePath   파일 경로
     * @return 존재 여부
     */
    boolean exists(String taskCd, String repoName, String commitHash, String filePath);
}
