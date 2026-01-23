package io.jgitkins.server.infrastructure.config.filter;

import io.jgitkins.server.infrastructure.config.git.GitSmartHttpPathForwardFilter;
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

    @Bean
    public FilterRegistrationBean<HttpLogFilter> httpLogFilterRegistration(HttpLogFilter httpLogFilter){
        FilterRegistrationBean<HttpLogFilter> filterRegistry = new FilterRegistrationBean<>();
        filterRegistry.setFilter(httpLogFilter);
        filterRegistry.addUrlPatterns("/*");
        filterRegistry.setOrder(-200); // 최상위
        return filterRegistry;
    }

    @Bean
    public GitSmartHttpPathForwardFilter gitSmartHttpPathForwardFilter() {
        return new GitSmartHttpPathForwardFilter();
    }

    @Bean
    public FilterRegistrationBean<GitSmartHttpPathForwardFilter> gitSmartHttpPathForwardFilterRegistration(
            GitSmartHttpPathForwardFilter gitSmartHttpPathForwardFilter
    ){
        FilterRegistrationBean<GitSmartHttpPathForwardFilter> filterRegistry = new FilterRegistrationBean<>();
        filterRegistry.setFilter(gitSmartHttpPathForwardFilter);
        filterRegistry.addUrlPatterns("/*");
        filterRegistry.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        filterRegistry.setOrder(-50); // Security Filter Chain 이후
        return filterRegistry;
    }

}
