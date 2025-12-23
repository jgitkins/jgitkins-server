package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.application.service.GitRepositoryAccessService;
import io.jgitkins.server.infrastructure.config.git.GitRepositoryRequestParser.GitRepositoryRequest;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomAdvertiseRefsHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomPostUploadHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.CustomPreUploadHook;
import io.jgitkins.server.infrastructure.config.git.hook.fetch.RefLogger;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FetchEventUploadPackFactory implements UploadPackFactory<HttpServletRequest> {

    private final GitRepositoryAccessService gitRepositoryAccessService;
    private final GitRepositoryRequestParser requestParser = new GitRepositoryRequestParser();

    @Override
    public UploadPack create(HttpServletRequest req, Repository db) throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        authorizeRead(req);

        UploadPack up = new UploadPack(db);

        up.setAdvertiseRefsHook(new CustomAdvertiseRefsHook(req)); // 클라이언트 요청 로깅 request ip, which repo ...
        up.setRefFilter(new RefLogger()); // 서버가 클라이언트에 제공할 정보 로깅 (Refs)
        up.setPreUploadHook(new CustomPreUploadHook()); // 사용자의 Fetching Packfile 요청에 대한 로깅 (사용자가 요청한 wants, haves || 그래서 어떤 Packfile 구성이 도출되었는지)
        up.setPostUploadHook(new CustomPostUploadHook());
//        up.setProtocolV2Hook(new LoggingProtocolV2Hook());

        System.out.println("req.requestUri!!!: " + req.getRequestURI());
        System.out.println("req.queryStr: " + req.getQueryString());
        System.out.println("req.contextPath: " + req.getContextPath());

        return up;

    }

    private void authorizeRead(HttpServletRequest request) throws ServiceNotAuthorizedException {
        GitRepositoryRequest repoRequest = requestParser.parse(request);
        if (repoRequest == null) {
            log.warn("git fetch denied: invalid repository path uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Invalid repository path");
        }
        Long userId = resolveUserId();
        if (userId == null) {
            log.warn("git fetch denied: unauthenticated request uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Unauthenticated");
        }
        boolean allowed = gitRepositoryAccessService.canRead(
                repoRequest.organizeSlug(),
                repoRequest.repositoryPath(),
                userId
        );
        if (!allowed) {
            log.warn("git fetch denied: org=[{}] repo=[{}] userId=[{}]",
                    repoRequest.organizeSlug(),
                    repoRequest.repositoryPath(),
                    userId);
            throw new ServiceNotAuthorizedException("Access denied");
        }
        log.info("git fetch allowed: org=[{}] repo=[{}] userId=[{}]",
                repoRequest.organizeSlug(),
                repoRequest.repositoryPath(),
                userId);
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
