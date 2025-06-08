package com.attention.analysis.Access_Service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClassificationServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassificationServiceClient.class);
    private final WebClient webClient;
    private final String classificationServiceUrl;
    
    public ClassificationServiceClient(WebClient.Builder webClientBuilder,
                                     @Value("${service.classification.url}") String classificationServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.classificationServiceUrl = classificationServiceUrl;
        logger.info("Classification Service URL configurado: {}", classificationServiceUrl);
    }
    
    public void solicitarClasificacion(Long idConversacion) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("idConversacion", idConversacion);
            
            webClient.post()
                    .uri(classificationServiceUrl + "/classify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(e -> {
                        logger.error("Error al solicitar clasificación: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(
                        response -> logger.info("Clasificación solicitada exitosamente para conversación {}", idConversacion),
                        error -> logger.error("Error en la solicitud de clasificación", error)
                    );
                    
        } catch (Exception e) {
            logger.error("Error al enviar solicitud a Classification Service: {}", e.getMessage(), e);
        }
    }
}