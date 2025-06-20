package com.attention.analysis.sentiment_service.service;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.dto.WhatsappMessage;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.AvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.AvgSentimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SentimentService {

    private static final Logger logger = LoggerFactory.getLogger(SentimentService.class);
    private final OpenAIService openAIService;
    private final SentimentRepository sentimentRepository;
    private final AvgSentimentRepository avgSentimentRepository;
    
    public SentimentService(OpenAIService openAIService,
                        SentimentRepository sentimentRepository,
                        AvgSentimentRepository avgSentimentRepository) {
        this.openAIService = openAIService;
        this.sentimentRepository = sentimentRepository;
        this.avgSentimentRepository = avgSentimentRepository;
    }
    
    @Transactional
    public void procesarSentimiento(SentimentRequest request) {
        Long idConversacion = request.getIdConversacion();
        logger.info("Procesando análisis de sentimiento para conversación ID: {}", idConversacion);
        
        WhatsappMessage whatsappMessage = request.getWhatsappMessage();

        if (whatsappMessage.getValue() == null ||
            whatsappMessage.getValue().getMessages() == null ||
            whatsappMessage.getValue().getMessages().isEmpty()) {
            throw new IllegalArgumentException("Estructura de mensaje WhatsApp inválida");
        }

        WhatsappMessage.Message message = whatsappMessage.getValue().getMessages().get(0);
        String texto = message.getText() != null ? message.getText().getBody() : "";

        java.time.LocalDateTime fecha;
        String ts = message.getTimestamp();
        try {
            long epoch = Long.parseLong(ts);
            if (ts.length() > 10) {
                epoch /= 1000; // timestamp in milliseconds
            }
            fecha = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(epoch),
                    java.time.ZoneId.systemDefault());
        } catch (Exception e) {
            logger.warn("Timestamp inválido '{}' - se usará el momento actual", ts);
            fecha = java.time.LocalDateTime.now();
        }

        logger.info("Analizando sentimiento del mensaje recibido: {} caracteres", texto.length());

        Integer valorSentimiento = openAIService.analizarSentimiento(texto);

        Sentiment sentiment = new Sentiment();
        sentiment.setIdConversacion(idConversacion);
        sentiment.setContenidoMensaje(texto);
        sentiment.setSentimiento(valorSentimiento);
        sentiment.setFechaEnvio(fecha);
        
        sentimentRepository.save(sentiment);
        logger.info("Análisis de sentimiento guardado: ID={}, valor={}", sentiment.getId(), valorSentimiento);
        
        // Calcular el promedio de los últimos 10 mensajes
        Pageable ultimosDiez = PageRequest.of(0, 10);
        List<Sentiment> ultimosMensajes = sentimentRepository
                .findLastMessagesByConversationId(idConversacion, ultimosDiez);
        
        double promedio = ultimosMensajes.stream()
                .mapToInt(Sentiment::getSentimiento)
                .average()
                .orElse(valorSentimiento);
        
        logger.info("Promedio de sentimiento calculado: {} basado en {} mensajes", 
                    promedio, ultimosMensajes.size());
        
        // Actualizar o insertar en la tabla avg_sentiment
        Optional<AvgSentiment> avgOptional = avgSentimentRepository
                .findByIdConversacion(idConversacion);
        
        AvgSentiment avgSentiment;
        if (avgOptional.isPresent()) {
            avgSentiment = avgOptional.get();
            logger.info("Actualizando registro existente en avg_sentiment para conversación: {}", idConversacion);
        } else {
            avgSentiment = new AvgSentiment();
            avgSentiment.setIdConversacion(idConversacion);
            logger.info("Creando nuevo registro en avg_sentiment para conversación: {}", idConversacion);
        }
        
        avgSentiment.setPromedioSentimiento(promedio);
        avgSentiment.setFechaUltimoMensaje(fecha);

        avgSentimentRepository.save(avgSentiment);
        logger.info("Registro avg_sentiment guardado con éxito. Promedio: {}", promedio);
    }
    
    // Método para análisis individual de mensajes (compatibilidad)
    @Transactional
    public void analizarMensaje(MensajeDTO mensajeDTO) {
        logger.info("Analizando mensaje individual para conversación: {}", 
                   mensajeDTO.getConversacion().getId());
        
        Integer valorSentimiento = openAIService.analizarSentimiento(mensajeDTO.getMensaje());
        
        Sentiment sentiment = new Sentiment();
        sentiment.setIdEmpresa(mensajeDTO.getConversacion().getIdEmpresa());
        sentiment.setIdConversacion(mensajeDTO.getConversacion().getId());
        sentiment.setContenidoMensaje(mensajeDTO.getMensaje());
        sentiment.setSentimiento(valorSentimiento);
        sentiment.setFechaEnvio(mensajeDTO.getFecha());
        
        sentimentRepository.save(sentiment);
        logger.info("Análisis de sentimiento individual guardado: valor={}", valorSentimiento);
    }
}