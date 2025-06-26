package com.example.classification_service.service;

import com.example.classification_service.dto.OpenAIRequest;
import com.example.classification_service.dto.OpenAIResponse;
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
            Actúas como un agente experto en análisis de conversaciones, con alta capacidad para evaluar mensajes utilizando lógica, criterio y sentido común. Tu tarea es clasificar la urgencia de cada conversación de manera objetiva. No te dejes guiar únicamente por las palabras utilizadas por el cliente; tú decides si la situación es realmente urgente o no.

            Solo puedes asignar una de las siguientes tres categorías a cada conversación: URGENTE, MODERADA, LEVE

            Para tomar tu decisión, analiza cuidadosamente los siguientes factores:

            ¿El cliente está exagerando o dramatizando la situación sin justificación real?

            ¿El mensaje del cliente es coherente, lógico y tiene fundamentos?

            ¿El contenido representa un riesgo real para la empresa, ya sea económico, legal o en términos de reputación pública?

            ¿El cliente está haciendo una pregunta por primera vez?

            → Clasifícalo como LEVE, sin importar el contenido.

            ¿El cliente ha repetido muchas veces una misma pregunta sin recibir respuesta?

            → Clasifícalo como URGENTE.

            ¿El cliente ha repetido muchas veces una pregunta ya respondida recientemente?

            → Clasifícalo como LEVE (indica comportamiento insistente sin justificación).

            ¿El cliente repite mensajes para llamar la atención?

            Evalúa el motivo real de su insistencia:

            Si el motivo es válido y crítico, clasifícalo como URGENTE.

            Si es importante pero no crítico, clasifícalo como MODERADA.

            Si el motivo es menor o irrelevante, clasifícalo como LEVE, sin importar la insistencia.
            
            **Enfócate principalmente en los mensajes más recientes** para determinar el estado actual de la conversación.
            
            **No te dejes influenciar por el tono emocional del cliente, o la insistencia sin motivo** Tu objetivo es clasificar la urgencia de la conversación de manera objetiva, basándote en los hechos y el contexto proporcionado.

            Tu respuesta debe ser ÚNICAMENTE una de estas tres palabras: URGENTE, MODERADA o LEVE.
            No agregues explicaciones ni texto adicional.
            """;
        
        String userPrompt = "Clasifica la siguiente conversación basándote en los mensajes más recientes:\n\n" + conversacionFormateada;
        
        List<OpenAIRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIRequest.Message("system", systemPrompt));
        messages.add(new OpenAIRequest.Message("user", userPrompt));
        
        OpenAIRequest request = new OpenAIRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setTemperature(0.1); // Baja temperatura para respuestas más consistentes
        request.setMaxTokens(10); // Solo necesitamos una palabra
        
        try {
            logger.debug("Enviando solicitud a OpenAI para clasificación...");
            
            OpenAIResponse response = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .onErrorResume(error -> {
                        logger.error("Error en llamada a OpenAI: {}", error.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String clasificacion = response.getChoices().get(0).getMessage().getContent().trim().toUpperCase();
                logger.info("Clasificación obtenida de OpenAI: {}", clasificacion);
                
                // Validar que la respuesta sea una de las clasificaciones esperadas
                if (clasificacion.equals("URGENTE") || clasificacion.equals("MODERADA") || clasificacion.equals("LEVE")) {
                    return clasificacion;
                } else {
                    logger.warn("Clasificación no válida recibida: {}. Usando MODERADA por defecto.", clasificacion);
                    return "MODERADA";
                }
            }
            
            logger.error("No se obtuvo respuesta válida de OpenAI");
            return "MODERADA"; // Valor por defecto
            
        } catch (Exception e) {
            logger.error("Error al clasificar con OpenAI: {}", e.getMessage(), e);
            return "MODERADA"; // Valor por defecto en caso de error
        }
    }
    
    /**
     * Método para obtener información sobre el uso de tokens (opcional)
     */
    public String obtenerInformacionModelo() {
        return String.format("Usando modelo: %s", model);
    }
}