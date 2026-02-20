package io.jgitkins.server.infrastructure.config.filter;

import io.jgitkins.server.infrastructure.config.git.GitSmartHttpCanonicalRedirectFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class FilterOrderConfig {

    // 1순위 필터 (Security Filter Chain 이 -100)
    @Bean
    public HttpLogFilter httpLogFilter() {
        return new HttpLogFilter();
    }

    // 디버깅 필터
    @Bean
    public FilterRegistrationBean<HttpLogFilter> httpLogFilterRegistration(HttpLogFilter httpLogFilter){
        FilterRegistrationBean<HttpLogFilter> filterRegistry = new FilterRegistrationBean<>();
        filterRegistry.setFilter(httpLogFilter);
        filterRegistry.addUrlPatterns("/*");
        filterRegistry.setOrder(-200); // 최상위
        return filterRegistry;
    }

    @Bean
    public GitSmartHttpCanonicalRedirectFilter gitSmartHttpCanonicalRedirectFilter() {
        return new GitSmartHttpCanonicalRedirectFilter();
    }

    // Git 요청 Filter
    @Bean
    public FilterRegistrationBean<GitSmartHttpCanonicalRedirectFilter> gitSmartHttpCanonicalRedirectFilterRegistration(
            GitSmartHttpCanonicalRedirectFilter gitSmartHttpCanonicalRedirectFilter
    ){
        FilterRegistrationBean<GitSmartHttpCanonicalRedirectFilter> filterRegistry = new FilterRegistrationBean<>();
        filterRegistry.setFilter(gitSmartHttpCanonicalRedirectFilter);
        filterRegistry.addUrlPatterns("/*");
        filterRegistry.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        filterRegistry.setOrder(-150); // Security Filter Chain 이전에 canonical redirect 수행
        return filterRegistry;
    }

}
