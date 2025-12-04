package io.jgitkins.server.application.port.out;

import java.util.Optional;

public interface UserLookupPort {
    /**
     * 사용자명으로 Users 테이블의 식별자를 조회
     *
     * @param username 시스템 로그인 사용자명
     * @return 사용자 PK
     */
    Optional<Long> findUserIdByUsername(String username);
}
