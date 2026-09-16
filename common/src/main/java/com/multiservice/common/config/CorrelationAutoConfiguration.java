package com.multiservice.common.config;

import com.multiservice.common.util.CorrelationIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name="correlation.id.enabled", havingValue = "true")
public class CorrelationAutoConfiguration {

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter(){

        FilterRegistrationBean<CorrelationIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorrelationIdFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}

