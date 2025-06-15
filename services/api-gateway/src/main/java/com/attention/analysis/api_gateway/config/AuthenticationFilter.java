package com.attention.analysis.api_gateway.config;

import com.attention.analysis.api_gateway.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Verificar si la ruta es pública
        if (isPublicRoute(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        // Verificar si el token está presente
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            // Verificar cookies como alternativa
            String tokenFromCookie = getTokenFromCookie(request);
            if (tokenFromCookie == null) {
                return onError(exchange, "Token de autorización faltante", HttpStatus.UNAUTHORIZED);
            }
            
            // Validar token de cookie
            if (!jwtService.isTokenValid(tokenFromCookie)) {
                return onError(exchange, "Token inválido", HttpStatus.UNAUTHORIZED);
            }
            
            // Agregar token al header para servicios downstream
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFromCookie)
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Token de autorización inválido", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        
        try {
            if (!jwtService.isTokenValid(token)) {
                return onError(exchange, "Token expirado o inválido", HttpStatus.UNAUTHORIZED);
            }
            
            // Opcional: Agregar información del usuario al header
            String username = jwtService.extractUsername(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", username)
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            return onError(exchange, "Error validando token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/webhook/") ||
               path.startsWith("/api/messages/webhook") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/wsp/") ||
               path.startsWith("/executive/") ||
               path.startsWith("/socket.io/");
    }

    private String getTokenFromCookie(ServerHttpRequest request) {
        if (request.getCookies().containsKey("jwt_token")) {
            return request.getCookies().getFirst("jwt_token").getValue();
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        // Agregar header Content-Type para JSON
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\": \"" + message + "\", \"status\": " + httpStatus.value() + "}";
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}