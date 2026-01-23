package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.application.service.GitRepositoryAccessService;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomAdvertiseRefsHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomPostUploadHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomPreUploadHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.RefLogger;
import io.jgitkins.server.infrastructure.config.security.filter.GitAuthChallengeFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FetchEventUploadPackFactory implements UploadPackFactory<HttpServletRequest> {

    private final GitRepositoryAccessService gitRepositoryAccessService;
    private final GitRequestAuthSupport requestAuthSupport;

    /***
     * Fetch Event Listener
     */
    @Override
    public UploadPack create(HttpServletRequest req, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        authorizeCanRead(req);

        UploadPack up = new UploadPack(db);

        up.setAdvertiseRefsHook(new CustomAdvertiseRefsHook(req)); // 클라이언트 요청 로깅 request ip, which repo ...
        up.setRefFilter(new RefLogger()); // 서버가 클라이언트에 제공할 정보 로깅 (Refs)
        up.setPreUploadHook(new CustomPreUploadHook()); // 사용자의 Fetching Packfile 요청에 대한 로깅 (사용자가 요청한 wants, haves || 그래서 어떤 Packfile 구성이 도출되었는지)
        up.setPostUploadHook(new CustomPostUploadHook());

        return up;
    }

    private void authorizeCanRead(HttpServletRequest request) throws ServiceNotAuthorizedException {
        GitSmartHttpEvent fetchEvent = GitSmartHttpEventParser.parse(request);
        if (fetchEvent == null) {
            log.warn("git fetch denied: invalid repository path uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Invalid repository path");
        }

        Object publicAttr = request.getAttribute(GitAuthChallengeFilter.REPO_PUBLIC_ATTR);
        if (Boolean.TRUE.equals(publicAttr)) {
            log.info("git fetch allowed (public repository): owner=[{}] repo=[{}]",
                    fetchEvent.ownerName(), fetchEvent.repositoryName());
            return;
        }

        log.info("the repository is private [{}] will be authorization soon", fetchEvent.repositoryName());
        Long userId = requestAuthSupport.resolveUserId(request);
        log.debug("authorized userId: [{}]", userId);
        if (userId == null) {
            log.warn("git fetch denied: unauthenticated request uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Unauthenticated");
        }

        boolean allowed = gitRepositoryAccessService.canRead(null,
                                                             fetchEvent.ownerName(),
                                                             fetchEvent.repositoryName(),
                                                             userId);
        if (!allowed) {
            log.warn("git fetch denied: owner=[{}] repo=[{}] userId=[{}]", fetchEvent.ownerName(), fetchEvent.repositoryName(), userId);
            throw new ServiceNotAuthorizedException("Access denied");
        }
        log.info("git fetch allowed: owner=[{}] repo=[{}] userId=[{}]", fetchEvent.ownerName(), fetchEvent.repositoryName(), userId);
    }

}
