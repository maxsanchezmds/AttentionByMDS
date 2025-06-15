package com.attention.analysis.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    @PostMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de autenticación no disponible");
        response.put("message", "El servicio de autenticación está temporalmente fuera de línea. Intente nuevamente en unos minutos.");
        response.put("service", "auth-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/messages")
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> messagesFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de mensajes no disponible");
        response.put("message", "El servicio de mensajes está temporalmente fuera de línea. Los mensajes se procesarán cuando el servicio esté disponible.");
        response.put("service", "message-receptionist");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/sentiment")
    @PostMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> sentimentFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de análisis de sentimiento no disponible");
        response.put("message", "El análisis de sentimiento está temporalmente fuera de línea. Los análisis se procesarán cuando el servicio esté disponible.");
        response.put("service", "sentiment-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/classification")
    @PostMapping("/classification")
    public ResponseEntity<Map<String, Object>> classificationFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de clasificación no disponible");
        response.put("message", "El servicio de clasificación está temporalmente fuera de línea. Las clasificaciones se procesarán cuando el servicio esté disponible.");
        response.put("service", "classification-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/access")
    @PostMapping("/access")
    public ResponseEntity<Map<String, Object>> accessFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de control de acceso no disponible");
        response.put("message", "El servicio de control de acceso está temporalmente fuera de línea. Se denegará el acceso hasta que el servicio esté disponible.");
        response.put("service", "access-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/chat")
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de chat no disponible");
        response.put("message", "El servicio de chat en tiempo real está temporalmente fuera de línea.");
        response.put("service", "chat-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/attention-quality")
    @PostMapping("/attention-quality")
    public ResponseEntity<Map<String, Object>> attentionQualityFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Servicio de calidad de atención no disponible");
        response.put("message", "El servicio de calidad de atención está temporalmente fuera de línea.");
        response.put("service", "attention-quality-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}