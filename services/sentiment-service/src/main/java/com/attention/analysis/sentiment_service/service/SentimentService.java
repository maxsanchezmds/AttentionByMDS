package com.attention.analysis.sentiment_service.service;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
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

    private final MessageReceptionistClient messageReceptionistClient;
    private final OpenAIService openAIService;
    private final SentimentRepository sentimentRepository;
    private final SvgSentimentRepository svgSentimentRepository;
    
    public SentimentService(MessageReceptionistClient messageReceptionistClient,
                          OpenAIService openAIService,
                          SentimentRepository sentimentRepository,
                          SvgSentimentRepository svgSentimentRepository) {
        this.messageReceptionistClient = messageReceptionistClient;
        this.openAIService = openAIService;
        this.sentimentRepository = sentimentRepository;
        this.svgSentimentRepository = svgSentimentRepository;
    }
    
    @Transactional
    public void procesarSentimiento(SentimentRequest request) {
        Long idConversacion = request.getIdConversacion();
        logger.info("Procesando análisis de sentimiento para conversación ID: {}", idConversacion);
        
        // Obtener todos los mensajes de la conversación desde message-receptionist
        List<MensajeDTO> todosMensajes = messageReceptionistClient.obtenerMensajesConversacion(idConversacion);
        
        if (todosMensajes == null || todosMensajes.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron mensajes para la conversación " + idConversacion);
        }
        
        // Obtener el último mensaje (el más reciente)
        MensajeDTO ultimoMensaje = todosMensajes.stream()
                .max((m1, m2) -> m1.getFecha().compareTo(m2.getFecha()))
                .orElseThrow(() -> new IllegalArgumentException("No se pudo determinar el último mensaje"));
        
        logger.info("Analizando sentimiento del último mensaje: {} caracteres, fecha: {}", 
                   ultimoMensaje.getMensaje().length(), ultimoMensaje.getFecha());
        
        // Analizar el sentimiento del último mensaje usando OpenAI
        Integer valorSentimiento = openAIService.analizarSentimiento(ultimoMensaje.getMensaje());
        
        // Guardar el análisis individual en la tabla sentiment
        Sentiment sentiment = new Sentiment();
        sentiment.setIdEmpresa(ultimoMensaje.getConversacion().getIdEmpresa());
        sentiment.setIdConversacion(idConversacion); // Ahora es Long
        sentiment.setContenidoMensaje(ultimoMensaje.getMensaje());
        sentiment.setSentimiento(valorSentimiento);
        sentiment.setFechaEnvio(ultimoMensaje.getFecha());
        
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
        
        // Actualizar o insertar en la tabla svg_sentiment
        Optional<SvgSentiment> svgOptional = svgSentimentRepository
                .findByIdConversacion(idConversacion);
        
        SvgSentiment svgSentiment;
        if (svgOptional.isPresent()) {
            svgSentiment = svgOptional.get();
            logger.info("Actualizando registro existente en svg_sentiment para conversación: {}", idConversacion);
        } else {
            svgSentiment = new SvgSentiment();
            svgSentiment.setIdConversacion(idConversacion); // Ahora es Long
            svgSentiment.setIdEmpresa(ultimoMensaje.getConversacion().getIdEmpresa());
            logger.info("Creando nuevo registro en svg_sentiment para conversación: {}", idConversacion);
        }
        
        svgSentiment.setPromedioSentimiento(promedio);
        svgSentiment.setFechaUltimoMensaje(ultimoMensaje.getFecha());
        
        svgSentimentRepository.save(svgSentiment);
        logger.info("Registro svg_sentiment guardado con éxito. Promedio: {}", promedio);
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