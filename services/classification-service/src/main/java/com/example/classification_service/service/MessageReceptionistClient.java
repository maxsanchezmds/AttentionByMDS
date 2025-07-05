package com.example.classification_service.service;

import com.example.classification_service.dto.MensajeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class MessageReceptionistClient {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageReceptionistClient.class);
    private final WebClient webClient;
    private final String messageReceptionistUrl;
    
    public MessageReceptionistClient(WebClient.Builder webClientBuilder,
                                   @Value("${service.message-receptionist.url}") String messageReceptionistUrl) {
        this.webClient = webClientBuilder.build();
        this.messageReceptionistUrl = messageReceptionistUrl;
        logger.info("Message Receptionist URL configurado: {}", messageReceptionistUrl);
    }
    
    public List<MensajeDTO> obtenerMensajesConversacion(Long idConversacion) {
        String url = messageReceptionistUrl + "/webhook/conversacion/" + idConversacion + "/mensajes";
        logger.info("Consultando mensajes de conversación en: {}", url);
        
        try {
            String respuesta = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(WebClientResponseException.class, e -> {
                        logger.error("Error HTTP al obtener mensajes: {} - {}",
                                   e.getRawStatusCode(), e.getResponseBodyAsString());
                        return Mono.empty();
                    })
                    .block();

            if (respuesta == null) {
                return List.of();
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(respuesta);
            JsonNode embedded = root.path("_embedded");
            if (embedded.isMissingNode() || !embedded.fieldNames().hasNext()) {
                return List.of();
            }
            JsonNode listaMensajes = embedded.elements().next();
            return mapper.convertValue(listaMensajes, new TypeReference<List<MensajeDTO>>() {});
        } catch (Exception e) {
            logger.error("Error al obtener mensajes de la conversación {}: {}",
                       idConversacion, e.getMessage(), e);
            throw new RuntimeException("No se pudieron obtener los mensajes de la conversación");
        }
    }
}