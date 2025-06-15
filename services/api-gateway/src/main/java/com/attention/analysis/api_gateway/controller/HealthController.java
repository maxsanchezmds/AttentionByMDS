package com.attention.analysis.api_gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String port;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", applicationName);
        health.put("status", "UP");
        health.put("port", port);
        health.put("timestamp", LocalDateTime.now());
        health.put("message", "API Gateway funcionando correctamente");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", applicationName);
        info.put("version", "1.0.0");
        info.put("description", "API Gateway para microservicios de ATTENTIONBYMDS");
        info.put("port", port);
        
        // Información de rutas disponibles
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/auth/**", "Servicio de autenticación");
        routes.put("/webhook/**", "Webhooks de WhatsApp");
        routes.put("/api/messages/**", "Servicio de mensajes");
        routes.put("/api/access/**", "Servicio de control de acceso");
        routes.put("/api/sentiment/**", "Servicio de análisis de sentimiento");
        routes.put("/api/classification/**", "Servicio de clasificación");
        routes.put("/api/attention-quality/**", "Servicio de calidad de atención");
        routes.put("/api/chat/**", "Servicio de chat en tiempo real");
        
        info.put("routes", routes);
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }
}