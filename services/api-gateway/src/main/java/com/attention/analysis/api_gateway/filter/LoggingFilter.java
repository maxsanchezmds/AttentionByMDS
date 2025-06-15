package com.attention.analysis.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Log de request entrante
        logger.info("🔄 [{}] {} {} {} - User-Agent: {}", 
                timestamp,
                request.getMethod(),
                request.getPath(),
                getClientIp(request),
                request.getHeaders().getFirst("User-Agent"));

        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                
                // Log de response
                String statusEmoji = getStatusEmoji(response.getStatusCode().value());
                logger.info("✅ [{}] {} {} {} - Status: {} {} - Duration: {}ms",
                        LocalDateTime.now().format(formatter),
                        request.getMethod(),
                        request.getPath(),
                        getClientIp(request),
                        response.getStatusCode().value(),
                        statusEmoji,
                        duration);
            })
        );
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
               request.getRemoteAddress().getAddress().getHostAddress() : 
               "unknown";
    }

    private String getStatusEmoji(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "✅";
        } else if (statusCode >= 300 && statusCode < 400) {
            return "↩️";
        } else if (statusCode >= 400 && statusCode < 500) {
            return "❌";
        } else if (statusCode >= 500) {
            return "💥";
        }
        return "❓";
    }

    @Override
    public int getOrder() {
        return -1; // Ejecutar antes que otros filtros
    }
}