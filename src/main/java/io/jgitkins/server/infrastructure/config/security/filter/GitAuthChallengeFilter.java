package io.jgitkins.server.infrastructure.config.security.filter;

import io.jgitkins.server.application.service.GitRepositoryAccessService;
import io.jgitkins.server.infrastructure.config.git.GitSmartHttpEvent;
import io.jgitkins.server.infrastructure.config.git.GitSmartHttpEventParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitAuthChallengeFilter extends OncePerRequestFilter {

    public static final String REPO_PUBLIC_ATTR = "jgitkins.repo.public";

    private final GitRepositoryAccessService gitRepositoryAccessService;

    /***
     * smart http 에 대응되는 filter
     * git fetch, push 이벤트 발생시 동작하는 filter
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        // resolve repository info from fetch or push event
        GitSmartHttpEvent repoRequest = GitSmartHttpEventParser.parse(request);
        if (repoRequest == null) {
            // Non-git or malformed paths should be handled by downstream handlers.
            filterChain.doFilter(request, response);
            return;
        }

        // check visibility
        Optional<Boolean> isPublic = gitRepositoryAccessService.resolveVisibility(null,
                                                                                  repoRequest.ownerName(),
                                                                                  repoRequest.repositoryName());

        // Unknown repo: let GitServlet resolve to 404 without leaking auth details.
        if (isPublic.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Public repos don't require auth challenge.
        request.setAttribute(REPO_PUBLIC_ATTR, isPublic.get());
        if (isPublic.get()) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            log.debug("git auth challenge: private repo access without credentials uri=[{}]",
                    request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"JGITKINS\"");
            return;
        }

        // Authorization exists: allow downstream handlers to authenticate and authorize.
        filterChain.doFilter(request, response);
    }
}
