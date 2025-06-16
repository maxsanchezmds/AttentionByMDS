package com.attention.analysis.api_gateway.config;

import com.attention.analysis.api_gateway.filter.RateLimitingFilter;
import com.attention.analysis.api_gateway.config.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class GatewayConfig {

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${services.message-receptionist.url}")
    private String messageReceptionistUrl;

    @Value("${services.access.url}")
    private String accessServiceUrl;

    @Value("${services.sentiment.url}")
    private String sentimentServiceUrl;

    @Value("${services.classification.url}")
    private String classificationServiceUrl;

    @Value("${services.attention-quality.url}")
    private String attentionQualityServiceUrl;

    @Value("${services.chat.url}")
    private String chatServiceUrl;

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // AUTH SERVICE - Rutas de autenticación (públicas con rate limiting)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f.filter(new RateLimitingFilter().apply(
                                new RateLimitingFilter.Config() {{
                                    setMaxRequests(50);
                                    setWindowSizeMs(60000);
                                }})))
                        .uri(authServiceUrl))
                
                // MESSAGE RECEPTIONIST - Webhook de WhatsApp (público)
                .route("message-receptionist-webhook", r -> r
                        .path("/webhook/**")
                        .filters(f -> f
                                .removeRequestHeader("X-Forwarded-Host")
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(messageReceptionistUrl))
                
                // MESSAGE RECEPTIONIST - Otras rutas (protegidas)
                .route("message-receptionist", r -> r
                        .path("/api/messages/**")
                        .filters(f -> f
                                .filter(authenticationFilter)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(messageReceptionistUrl))
                
                // ACCESS SERVICE (protegido)
                .route("access-service", r -> r
                        .path("/api/access/**")
                        .filters(f -> f
                                .filter(authenticationFilter)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(accessServiceUrl))
                
                // SENTIMENT SERVICE (protegido)
                .route("sentiment-service", r -> r
                        .path("/api/sentiment/**")
                        .filters(f -> f
                                .filter(authenticationFilter)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(sentimentServiceUrl))
                
                // CLASSIFICATION SERVICE (protegido)
                .route("classification-service", r -> r
                        .path("/api/classification/**")
                        .filters(f -> f
                                .filter(authenticationFilter)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(classificationServiceUrl))
                
                // ATTENTION QUALITY SERVICE (protegido)
                .route("attention-quality-service", r -> r
                        .path("/api/attention-quality/**")
                        .filters(f -> f
                                .filter(authenticationFilter)
                                .addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(attentionQualityServiceUrl))
                
                // CHAT SERVICE (puede necesitar WebSocket)
                .route("chat-service", r -> r
                        .path("/api/chat/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(chatServiceUrl))
                
                // CHAT SERVICE WebSocket endpoints
                .route("chat-service-ws", r -> r
                        .path("/socket.io/**")
                        .filters(f -> f.addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(chatServiceUrl))
                
                // Ruta para endpoints específicos de WhatsApp
                .route("whatsapp-webhook", r -> r
                        .path("/wsp")
                        .filters(f -> f.addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(chatServiceUrl))
                
                .route("executive-chat", r -> r
                        .path("/executive")
                        .filters(f -> f.addRequestHeader("X-Gateway-Source", "api-gateway"))
                        .uri(chatServiceUrl))
                
                .build();
    }
}