package com.multiservice.gateway_service.config;


import com.multiservice.gateway_service.filter.ForwardAuthHeaderFilter;
import com.multiservice.gateway_service.filter.LoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GatewayConfig {

    @Bean
    public FilterRegistrationBean<ForwardAuthHeaderFilter> registerForwardAuthHeaderFilter(ForwardAuthHeaderFilter filter){

        FilterRegistrationBean<ForwardAuthHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(0); //it runs before routing
        return registration;

    }


    @Bean
    public FilterRegistrationBean<LoggingFilter> registerLoggingFilter(LoggingFilter filter){

        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        return registration;
    }
}
