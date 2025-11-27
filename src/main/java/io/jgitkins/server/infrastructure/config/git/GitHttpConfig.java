package io.jgitkins.server.infrastructure.config.git;

import java.io.File;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.FileResolver;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class GitHttpConfig {

    // TODO: 환경변수화 필요
    private final String ROOT_PATH = String.format("%s/%s", System.getProperty("user.home"), "tmptmp/bare");
    private final File REPO_ROOT = new File(ROOT_PATH); // 저장소 루트 경로
    private final FetchEventUploadPackFactory fetchEventUploadPackFactory;
    private final PushEventReceivePackFactory pushEventReceivePackFactory;


    // Git Servlet Configuration
    @Bean
    public ServletRegistrationBean<GitServlet> gitServlet() {
        GitServlet servlet = new GitServlet();

        RepositoryResolver<HttpServletRequest> resolver = new FileResolver<>(REPO_ROOT, true); // false → exportAll=false
        servlet.setRepositoryResolver(resolver);


        // When Occur Fetch Event (Pull, Clone)
//        servlet.setUploadPackFactory((req, db) -> {
//            UploadPack up = new UploadPack(db);
//
//            up.setAdvertiseRefsHook(new AdvertiseRefsLogger(req));
//            up.setRefFilter(new RefLogger());
//            up.setPreUploadHook(new NegotiationEventLogger()); // 사용자의 Fetching Packfile 요청에 대한 로깅
//            up.setPostUploadHook(new LoggingPostUploadHook());
//            up.setProtocolV2Hook(new LoggingProtocolV2Hook());
//
//            System.out.println("req.requestUri!!!: " + req.getRequestURI());
//            System.out.println("req.queryStr: " + req.getQueryString());
//            System.out.println("req.contextPath: " + req.getContextPath());
//
//
//            return up;
//        });
        servlet.setUploadPackFactory(fetchEventUploadPackFactory); // Fetch 이벤트 발생에대한 로깅 설정
        servlet.setReceivePackFactory(pushEventReceivePackFactory);

//        servlet.setReceivePackFactory((req, db) -> {
//            ReceivePack rp = new ReceivePack(db);
//
//            // 브랜치 신규 생성 Listener (브랜치 관리 가능)
//            rp.setPostReceiveHook(new LoggingPostReceiveHook(req));
//
//            return rp;
//        }); // push

        return new ServletRegistrationBean<>(servlet, "/git/*");
    }

//    private String authorization(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return (String) authentication.getPrincipal();
//    }
}
