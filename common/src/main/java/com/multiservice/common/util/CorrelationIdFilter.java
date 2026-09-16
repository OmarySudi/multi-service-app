package com.multiservice.common.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final String CORRELATION_ID_HEADER = "X-Correlation-ID";

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String correlationId = httpReq.getHeader(CORRELATION_ID_HEADER);

        if(correlationId == null || correlationId.isBlank()){
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation ID: {}", correlationId);
        } else {
            log.debug("Incoming Correlation ID: {}", correlationId);
        }

        // Add correlation ID to MDC logging
        org.slf4j.MDC.put("correlationId",correlationId);

        // WRAP REQUEST AND INSERT HEADER
        String finalCorrelationId = correlationId;

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpReq) {

            @Override
            public String getHeader(String name) {
                if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                    return finalCorrelationId;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                    return Collections.enumeration(Collections.singletonList(finalCorrelationId));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (!names.contains(CORRELATION_ID_HEADER)) {
                    names.add(CORRELATION_ID_HEADER);
                }
                return Collections.enumeration(names);
            }
        };

        // Also set it on response (optional but useful)
        httpRes.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            // Use WRAPPED request (VERY IMPORTANT)
            chain.doFilter(wrappedRequest, response);
        } finally {
            org.slf4j.MDC.remove("correlationId");
        }

    }
}
