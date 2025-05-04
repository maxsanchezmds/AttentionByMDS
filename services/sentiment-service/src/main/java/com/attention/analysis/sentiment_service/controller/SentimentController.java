package com.attention.analysis.sentiment_service.controller;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
import com.attention.analysis.sentiment_service.service.SentimentAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/analizar")
public class SentimentController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private SentimentRepository sentimentRepository;

    @Autowired
    private SvgSentimentRepository svgSentimentRepository;
    @GetMapping("/analisis/{idConversacion}")
    public ResponseEntity<?> obtenerAnalisisPorConversacion(@PathVariable String idConversacion) {
        logger.info("Solicitando análisis para conversación: {}", idConversacion);
        
        // Buscar el promedio en svg_sentiment
        Optional<SvgSentiment> svgSentiment = svgSentimentRepository.findByIdConversacion(idConversacion);
        
        if (svgSentiment.isPresent()) {
            return ResponseEntity.ok(svgSentiment.get());
        } else {
            // Buscar análisis individuales
            List<Sentiment> sentimientos = sentimentRepository.findLastMessagesByConversationId(
                idConversacion, PageRequest.of(0, 10));
                
            if (!sentimientos.isEmpty()) {
                return ResponseEntity.ok(sentimientos);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }
    @PostMapping
    public ResponseEntity<String> analizarMensaje(@RequestBody MensajeDTO mensajeDTO) {
        logger.info("Recibida solicitud para analizar mensaje para conversación: {}", 
                   mensajeDTO.getId_conversacion());
        sentimentAnalysisService.analizarMensaje(mensajeDTO);
        return ResponseEntity.ok("Análisis de sentimiento iniciado correctamente");
    }
}