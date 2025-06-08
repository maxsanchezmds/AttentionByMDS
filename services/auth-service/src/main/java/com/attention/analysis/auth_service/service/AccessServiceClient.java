package com.attention.analysis.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${service.access.url}")
    private String accessServiceUrl;
    
    public void crearAccesosEmpresa(Long idEmpresa) {
        try {
            // Crear el body con el ID de la empresa y los accesos en false
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("idEmpresa", idEmpresa);
            requestBody.put("sentimentAccess", false);
            requestBody.put("classificationAccess", false);
            requestBody.put("attentionQualityAccess", false);
            requestBody.put("feedbackAccess", false);
            requestBody.put("learnAccess", false);
            
            WebClient webClient = webClientBuilder.build();
            
            webClient.post()
                    .uri(accessServiceUrl + "/empresa/crear-accesos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(e -> {
                        log.error("Error al crear accesos para empresa {}: {}", idEmpresa, e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(
                        response -> log.info("Accesos creados exitosamente para empresa {}", idEmpresa),
                        error -> log.error("Error en la creación de accesos", error)
                    );
                    
        } catch (Exception e) {
            log.error("Error al enviar solicitud a Access Service: {}", e.getMessage(), e);
        }
    }
}