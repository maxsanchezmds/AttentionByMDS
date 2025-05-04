package com.attention.analysis.sentiment_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.model}")
    private String model;
    
    public Integer analizarSentimiento(String textoMensaje) {
        try {
            logger.info("Analizando sentimiento para texto: {}", textoMensaje.substring(0, Math.min(50, textoMensaje.length())));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            // Prompt que instruye a ChatGPT a analizar el sentimiento
            String prompt = "Analiza el sentimiento del siguiente texto y califícalo en una escala de 0 a 100, donde:\n" +
                            "- 0 representa un sentimiento extremadamente negativo\n" +
                            "- 100 representa un sentimiento extremadamente positivo\n\n" +
                            "Solo debes responder con el número que representa tu análisis, seguido de '/100'. Por ejemplo: '60/100'.\n\n" +
                            "Texto a analizar: \"" + textoMensaje + "\"";
            
            // Configurar los mensajes para el API de ChatGPT
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Eres un analizador de sentimiento preciso que solo devuelve valores numéricos.");
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            
            requestBody.put("messages", new Object[]{systemMessage, userMessage});
            requestBody.put("temperature", 0.3); // Menor temperatura para respuestas más consistentes
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Enviar solicitud a la API de OpenAI
            String apiUrl = "https://api.openai.com/v1/chat/completions";
            String responseJson = restTemplate.postForObject(apiUrl, request, String.class);
            
            // Procesar la respuesta
            JsonNode rootNode = objectMapper.readTree(responseJson);
            String respuesta = rootNode.path("choices").get(0).path("message").path("content").asText().trim();
            
            logger.info("Respuesta recibida de OpenAI: {}", respuesta);
            
            // Extraer el número de la respuesta usando regex
            Pattern pattern = Pattern.compile("(\\d+)/100");
            Matcher matcher = pattern.matcher(respuesta);
            
            if (matcher.find()) {
                int valor = Integer.parseInt(matcher.group(1));
                logger.info("Valor de sentimiento extraído: {}", valor);
                return valor;
            } else {
                // Si no se encuentra el formato esperado, intentamos extraer cualquier número
                Pattern numPattern = Pattern.compile("\\d+");
                Matcher numMatcher = numPattern.matcher(respuesta);
                
                if (numMatcher.find()) {
                    int valor = Integer.parseInt(numMatcher.group());
                    // Asegurarse de que esté en el rango de 0 a 100
                    valor = Math.min(Math.max(valor, 0), 100);
                    logger.info("Valor de sentimiento extraído (formato alternativo): {}", valor);
                    return valor;
                }
                
                // Valor por defecto si no se puede extraer nada
                logger.warn("No se pudo extraer un valor numérico, usando valor neutral (50)");
                return 50;
            }
            
        } catch (Exception e) {
            logger.error("Error al analizar sentimiento: {}", e.getMessage(), e);
            // En caso de error, devolver un valor neutral
            return 50;
        }
    }
}