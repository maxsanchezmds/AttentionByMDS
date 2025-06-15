package com.attention.analysis.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lastRequestTimes = new ConcurrentHashMap<>();

    public RateLimitingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientId(exchange);
            
            if (isRateLimited(clientId, config)) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().add("Content-Type", "application/json");
                
                String body = "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests\"}";
                return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
            }
            
            return chain.filter(exchange);
        };
    }

    private String getClientId(org.springframework.web.server.ServerWebExchange exchange) {
        // Usar IP del cliente como identificador
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return exchange.getRequest().getRemoteAddress() != null ? 
               exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : 
               "unknown";
    }

    private boolean isRateLimited(String clientId, Config config) {
        long currentTime = System.currentTimeMillis();
        
        // Limpiar contadores antiguos
        AtomicLong lastTime = lastRequestTimes.get(clientId);
        if (lastTime != null && currentTime - lastTime.get() > config.getWindowSizeMs()) {
            requestCounts.remove(clientId);
            lastRequestTimes.remove(clientId);
        }
        
        // Actualizar contador y tiempo
        AtomicInteger count = requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0));
        lastRequestTimes.put(clientId, new AtomicLong(currentTime));
        
        int currentCount = count.incrementAndGet();
        
        return currentCount > config.getMaxRequests();
    }

    public static class Config {
        private int maxRequests = 100; // Máximo de requests por ventana
        private long windowSizeMs = 60000; // 1 minuto en milisegundos

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public long getWindowSizeMs() {
            return windowSizeMs;
        }

        public void setWindowSizeMs(long windowSizeMs) {
            this.windowSizeMs = windowSizeMs;
        }
    }
}