package com.attention.analysis.Access_Service.service;

import com.attention.analysis.Access_Service.dto.WhatsappMessage;
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
public class SentimentServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(SentimentServiceClient.class);
    private final WebClient webClient;
    private final String sentimentServiceUrl;
    
    public SentimentServiceClient(WebClient.Builder webClientBuilder,
                                @Value("${service.sentiment.url}") String sentimentServiceUrl) {
        this.webClient = webClientBuilder.build();
        this.sentimentServiceUrl = sentimentServiceUrl;
        logger.info("Sentiment Service URL configurado: {}", sentimentServiceUrl);
    }
    
    public void solicitarAnalisisSentimiento(Long idConversacion, WhatsappMessage whatsappMessage) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("idConversacion", idConversacion);
            request.put("whatsappMessage", whatsappMessage);
            
            webClient.post()
                    .uri(sentimentServiceUrl + "/procesar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(e -> {
                        logger.error("Error al solicitar análisis de sentimiento: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribe(
                        response -> logger.info("Análisis de sentimiento solicitado exitosamente para conversación {}", idConversacion),
                        error -> logger.error("Error en la solicitud de análisis de sentimiento", error)
                    );
                    
        } catch (Exception e) {
            logger.error("Error al enviar solicitud a Sentiment Service: {}", e.getMessage(), e);
        }
    }
}