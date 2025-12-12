package io.jgitkins.server.infrastructure.config.git;

import io.jgitkins.server.application.port.in.HandlePushEventUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import io.jgitkins.server.infrastructure.config.git.hook.push.PushHook;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushEventReceivePackFactory implements ReceivePackFactory<HttpServletRequest> {

    private final HandlePushEventUseCase handlePushEventUseCase;

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
        ReceivePack rp = new ReceivePack(db);

        // 브랜치 신규 생성 Listener (브랜치 관리 가능)
        rp.setPostReceiveHook(new PushHook(req, handlePushEventUseCase));

        return rp;

    }
}
