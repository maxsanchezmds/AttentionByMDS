package com.attention.analysis.sentiment_service.service;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SentimentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);

    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private SentimentRepository sentimentRepository;
    
    @Autowired
    private SvgSentimentRepository svgSentimentRepository;
    
    @Transactional
    public void analizarMensaje(MensajeDTO mensajeDTO) {
        logger.info("Iniciando análisis de mensaje para conversación: {}", mensajeDTO.getId_conversacion());
        
        // 1. Analizar el sentimiento del mensaje usando OpenAI
        Integer valorSentimiento = openAIService.analizarSentimiento(mensajeDTO.getContenido_mensaje());
        
        // 2. Guardar el análisis individual en la tabla sentiment
        Sentiment sentiment = new Sentiment();
        sentiment.setIdEmpresa(mensajeDTO.getId_empresa());
        sentiment.setIdConversacion(mensajeDTO.getId_conversacion());
        sentiment.setContenidoMensaje(mensajeDTO.getContenido_mensaje());
        sentiment.setSentimiento(valorSentimiento);
        sentiment.setFechaEnvio(mensajeDTO.getFecha_envio());
        
        sentimentRepository.save(sentiment);
        logger.info("Análisis de sentimiento guardado: {}, valor: {}", sentiment.getId(), valorSentimiento);
        
        // 3. Calcular el promedio de los últimos 10 mensajes
        Pageable ultimosDiez = PageRequest.of(0, 10);
        List<Sentiment> ultimosMensajes = sentimentRepository
                .findLastMessagesByConversationId(mensajeDTO.getId_conversacion(), ultimosDiez);
        
        double promedio = ultimosMensajes.stream()
                .mapToInt(Sentiment::getSentimiento)
                .average()
                .orElse(valorSentimiento);
        
        logger.info("Promedio de sentimiento calculado: {} basado en {} mensajes", 
                    promedio, ultimosMensajes.size());
        
        // 4. Actualizar o insertar en la tabla svg_sentiment
        Optional<SvgSentiment> svgOptional = svgSentimentRepository
                .findByIdConversacion(mensajeDTO.getId_conversacion());
        
        SvgSentiment svgSentiment;
        if (svgOptional.isPresent()) {
            svgSentiment = svgOptional.get();
            logger.info("Actualizando registro existente en svg_sentiment para conversación: {}", 
                       mensajeDTO.getId_conversacion());
        } else {
            svgSentiment = new SvgSentiment();
            svgSentiment.setIdConversacion(mensajeDTO.getId_conversacion());
            svgSentiment.setIdEmpresa(mensajeDTO.getId_empresa());
            logger.info("Creando nuevo registro en svg_sentiment para conversación: {}", 
                       mensajeDTO.getId_conversacion());
        }
        
        svgSentiment.setPromedioSentimiento(promedio);
        svgSentiment.setFechaUltimoMensaje(mensajeDTO.getFecha_envio());
        
        svgSentimentRepository.save(svgSentiment);
        logger.info("Registro svg_sentiment guardado con éxito. Promedio: {}", promedio);
    }
}