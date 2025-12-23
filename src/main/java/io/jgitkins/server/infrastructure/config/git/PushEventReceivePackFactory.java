package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.application.port.in.PushEventHandleUseCase;
import io.jgitkins.server.application.service.GitRepositoryAccessService;
import io.jgitkins.server.infrastructure.config.git.GitRepositoryRequestParser.GitRepositoryRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import io.jgitkins.server.infrastructure.config.git.hook.push.PushHook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushEventReceivePackFactory implements ReceivePackFactory<HttpServletRequest> {

    private final PushEventHandleUseCase pushEventHandleUseCase;
    private final GitRepositoryAccessService gitRepositoryAccessService;
    private final GitRepositoryRequestParser requestParser = new GitRepositoryRequestParser();

    // @Override
    // public UploadPack create(HttpServletRequest req, Repository db) {
    // UploadPack up = new UploadPack(db);
    //
    // up.setAdvertiseRefsHook(new AdvertiseRefsLogger(req));
    // up.setRefFilter(new RefLogger());
    // up.setPreUploadHook(new NegotiationEventLogger()); // 사용자의 Fetching Packfile
    // 요청에 대한 로깅
    // up.setPostUploadHook(new LoggingPostUploadHook());
    // up.setProtocolV2Hook(new LoggingProtocolV2Hook());
    //
    // System.out.println("req.requestUri!!!: " + req.getRequestURI());
    // System.out.println("req.queryStr: " + req.getQueryString());
    // System.out.println("req.contextPath: " + req.getContextPath());
    //
    // return up;
    //
    // }

    @Override
    public ReceivePack create(HttpServletRequest req, Repository db)
            throws ServiceNotEnabledException, ServiceNotAuthorizedException {
        authorizeWrite(req);
        ReceivePack rp = new ReceivePack(db);

        // 브랜치 신규 생성 Listener (브랜치 관리 가능)
        rp.setPostReceiveHook(new PushHook(req, pushEventHandleUseCase));

        return rp;

    }

    private void authorizeWrite(HttpServletRequest request) throws ServiceNotAuthorizedException {
        GitRepositoryRequest repoRequest = requestParser.parse(request);
        if (repoRequest == null) {
            log.warn("git push denied: invalid repository path uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Invalid repository path");
        }
        Long userId = resolveUserId();
        if (userId == null) {
            log.warn("git push denied: unauthenticated request uri=[{}]", request.getRequestURI());
            throw new ServiceNotAuthorizedException("Unauthenticated");
        }
        boolean allowed = gitRepositoryAccessService.canWrite(
                repoRequest.organizeSlug(),
                repoRequest.repositoryPath(),
                userId
        );
        if (!allowed) {
            log.warn("git push denied: org=[{}] repo=[{}] userId=[{}]",
                    repoRequest.organizeSlug(),
                    repoRequest.repositoryPath(),
                    userId);
            throw new ServiceNotAuthorizedException("Access denied");
        }
        log.info("git push allowed: org=[{}] repo=[{}] userId=[{}]",
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
