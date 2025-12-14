package com.multiservice.gateway_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // ------------------------------
        // Extract Request Information
        // ------------------------------
        String correlationId = MDC.get("correlationId");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = extractClientIp(request);
        boolean hasAuth = request.getHeader("Authorization") != null;

        long startTime = System.currentTimeMillis();


        // ------------------------------
        // Log inbound request
        // ------------------------------
        log.info("GATEWAY_INBOUND_REQUEST method={} path={} clientIp={} userAgent=\"{}\" authPresent={} correlationId={}",
                method, uri, clientIp, userAgent, hasAuth, correlationId);

        // Continue filter chain
        filterChain.doFilter(request, response);

        // ------------------------------
        // Compute latency
        // ------------------------------
        long durationMs = System.currentTimeMillis() - startTime;

        // ------------------------------
        // Log outbound response
        // ------------------------------
        log.info("GATEWAY_OUTBOUND_RESPONSE status={} path={} durationMs={} correlationId={}",
                response.getStatus(), uri, durationMs, correlationId);
    }


    // Extract real client IP: handles reverse proxies, NGINX, Load Balancers
    private String extractClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP",
                "X-Client-IP",
                "X-Forwarded",
                "Forwarded-For"
        };

        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isBlank()) {
                return ip.split(",")[0].trim(); // First IP in list
            }
        }

        return request.getRemoteAddr();
    }
}
