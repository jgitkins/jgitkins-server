package io.jgitkins.server.infrastructure.config.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
