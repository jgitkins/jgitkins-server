package io.jgitkins.server.infrastructure.config.git;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

public class GitSmartHttpPathForwardFilter extends OncePerRequestFilter {

    private static final Pattern ROOT_GIT_PATH = Pattern.compile("^/[^/]+/[^/]+\\.git(?:/.*)?$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request.getDispatcherType() == DispatcherType.FORWARD) {
            return true;
        }
        String path = resolvePath(request);
        if (path.startsWith("/git/")) {
            return true;
        }
        return !ROOT_GIT_PATH.matcher(path).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = resolvePath(request);
        request.getRequestDispatcher("/git" + path).forward(request, response);
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path;
    }
}
