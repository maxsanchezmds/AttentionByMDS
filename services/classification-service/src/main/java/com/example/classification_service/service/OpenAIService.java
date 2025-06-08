package com.attention.analysis.Classification_Service.service;

import com.attention.analysis.Classification_Service.dto.OpenAIRequest;
import com.attention.analysis.Classification_Service.dto.OpenAIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    
    public OpenAIService(WebClient.Builder webClientBuilder,
                        @Value("${openai.api.key}") String apiKey,
                        @Value("${openai.model:gpt-4-turbo-preview}") String model) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        logger.info("OpenAI Service configurado con modelo: {}", model);
    }
    
    public String clasificarConversacion(String conversacionFormateada) {
        String systemPrompt = """
            Eres un asistente especializado en clasificar conversaciones de atención al cliente.
            
            Debes clasificar cada conversación en una de estas tres categorías:
            - URGENTE: Situaciones que requieren atención inmediata
            - MODERADA: Situaciones que requieren atención pero no son críticas
            - LEVE: Consultas generales o situaciones de baja prioridad
            
            Para clasificar, considera:
            1. ¿Se le respondió al cliente? Si no tiene respuesta y el mensaje es de hace tiempo, puede ser urgente.
            2. ¿Cuál es el sentimiento del cliente? (negativo = más urgente, positivo = menos urgente)
            3. ¿El cliente expresa urgencia explícitamente? (palabras como "urgente", "ahora", "inmediato", etc.)
            4. ¿El cliente recibió respuesta pero sigue insistiendo? Esto puede indicar que no quedó satisfecho.
            
            Tu respuesta debe ser ÚNICAMENTE una de estas tres palabras: URGENTE, MODERADA o LEVE.
            No agregues explicaciones ni texto adicional.
            """;
        
        String userPrompt = "Clasifica la siguiente conversación:\n\n" + conversacionFormateada;
        
        List<OpenAIRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIRequest.Message("system", systemPrompt));
        messages.add(new OpenAIRequest.Message("user", userPrompt));
        
        OpenAIRequest request = new OpenAIRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setTemperature(0.1); // Baja temperatura para respuestas más consistentes
        request.setMaxTokens(10); // Solo necesitamos una palabra
        
        try {
            OpenAIResponse response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String clasificacion = response.getChoices().get(0).getMessage().getContent().trim().toUpperCase();
                logger.info("Clasificación obtenida: {}", clasificacion);
                return clasificacion;
            }
            
            throw new RuntimeException("No se obtuvo respuesta de OpenAI");
            
        } catch (Exception e) {
            logger.error("Error al clasificar con OpenAI: {}", e.getMessage(), e);
            throw new RuntimeException("Error al clasificar la conversación", e);
        }
    }
}