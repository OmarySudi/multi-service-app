package com.multiservice.gateway_service.config;

import com.multiservice.gateway_service.filter.CorrelationIdFilter;
import com.multiservice.gateway_service.filter.ForwardAuthHeaderFilter;
import com.multiservice.gateway_service.filter.LoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public FilterRegistrationBean<ForwardAuthHeaderFilter> registerForwardAuthHeaderFilter(ForwardAuthHeaderFilter filter){

        FilterRegistrationBean<ForwardAuthHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1); //it runs before routing
        return registration;

    }


    @Bean
    public FilterRegistrationBean<LoggingFilter> registerLoggingFilter(LoggingFilter filter){

        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> registerCorrelationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(0); // Should run first
        return registration;
    }

}
