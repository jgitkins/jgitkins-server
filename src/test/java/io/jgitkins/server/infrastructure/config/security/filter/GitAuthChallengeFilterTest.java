package io.jgitkins.server.infrastructure.config.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.jgitkins.server.application.service.GitRepositoryAccessService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class GitAuthChallengeFilterTest {

    @Mock
    private GitRepositoryAccessService gitRepositoryAccessService;

    private GitAuthChallengeFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GitAuthChallengeFilter(gitRepositoryAccessService);
    }

    @Test
    void publicUploadPack_withoutAuth_allowsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/git/hrk11mmmm/repo.git/info/refs");
        request.setQueryString("service=git-upload-pack");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(gitRepositoryAccessService.resolveVisibility(null, "hrk11mmmm", "repo"))
                .thenReturn(Optional.of(true));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void publicReceivePack_withoutAuth_returnsUnauthorizedChallenge() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/git/hrk11mmmm/repo.git/info/refs");
        request.setQueryString("service=git-receive-pack");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(gitRepositoryAccessService.resolveVisibility(null, "hrk11mmmm", "repo"))
                .thenReturn(Optional.of(true));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic realm=\"JGITKINS\"");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void privateUploadPack_withoutAuth_returnsUnauthorizedChallenge() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/git/hrk11mmmm/repo.git/info/refs");
        request.setQueryString("service=git-upload-pack");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(gitRepositoryAccessService.resolveVisibility(null, "hrk11mmmm", "repo"))
                .thenReturn(Optional.of(false));

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Basic realm=\"JGITKINS\"");
        assertThat(chain.getRequest()).isNull();
    }
}
