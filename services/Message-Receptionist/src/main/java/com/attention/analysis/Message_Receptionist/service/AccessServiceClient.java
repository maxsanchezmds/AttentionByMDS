package com.attention.analysis.Message_Receptionist.service;

import com.attention.analysis.Message_Receptionist.dto.TwilioMessage;
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
public class AccessServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessServiceClient.class);
    private final WebClient webClient;
    private final String accessServiceUrl;
    
    public AccessServiceClient(WebClient.Builder webClientBuilder,
                              @Value("${service.access.url}") String accessServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.accessServiceUrl = accessServiceUrl;
        logger.info("Access Service URL configurado: {}", accessServiceUrl);
    }
    
    public void notificarAcceso(Long idConversacion, TwilioMessage whatsappMessage) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("idConversacion", idConversacion);
            request.put("whatsappMessage", whatsappMessage);
            
            webClient.post()
                    .uri(accessServiceUrl + "/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(e -> {
                        logger.error("Error al notificar a Access Service: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(
                        response -> logger.info("Access Service notificado exitosamente"),
                        error -> logger.error("Error en la notificación a Access Service", error)
                    );
                    
        } catch (Exception e) {
            logger.error("Error al enviar notificación a Access Service: {}", e.getMessage(), e);
        }
    }
}