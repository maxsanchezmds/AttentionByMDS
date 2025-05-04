package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class SentimentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${sentiment.service.url:http://localhost:8082/api/sentiment}")
    private String sentimentServiceUrl;
    
    @Async
    public void enviarMensajeParaAnalisis(Message message) {
        try {
            logger.info("Enviando mensaje para análisis de sentimiento: {}", message.getId());
            
            // Preparar los datos para enviar
            Map<String, Object> datos = new HashMap<>();
            datos.put("contenido_mensaje", message.getMessageContent());
            datos.put("id_conversacion", message.getConversation().getId());
            datos.put("id_empresa", message.getConversation().getEmpresa().getIdEmpresa());
            datos.put("fecha_envio", message.getTimestamp());
            
            logger.info("Datos a enviar: {}", datos);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear la entidad HTTP
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(datos, headers);
            
            // Enviar la solicitud al servicio de análisis de sentimiento
            String url = sentimentServiceUrl + "/analizar";
            logger.info("Enviando request a: {}", url);
            
            restTemplate.postForEntity(url, request, String.class);
            
            logger.info("Mensaje enviado correctamente para análisis: {}", message.getId());
            
        } catch (Exception e) {
            logger.error("Error al enviar mensaje para análisis: {}", e.getMessage(), e);
        }
    }
}