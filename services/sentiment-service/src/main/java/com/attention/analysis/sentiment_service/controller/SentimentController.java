package com.attention.analysis.sentiment_service.controller;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.SvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.SvgSentimentRepository;
import com.attention.analysis.sentiment_service.service.SentimentAnalysisService;
import com.attention.analysis.sentiment_service.service.SentimentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/analizar")
public class SentimentController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;
    
    @Autowired
    private SentimentService sentimentService;

    @Autowired
    private SentimentRepository sentimentRepository;

    @Autowired
    private SvgSentimentRepository svgSentimentRepository;
    
    /**
     * NUEVO ENDPOINT: Recibe el mismo JSON que access-service envía a classification-service
     */
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarSentimiento(@Valid @RequestBody SentimentRequest request) {
        try {
            sentimentService.procesarSentimiento(request);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Análisis de sentimiento procesado exitosamente",
                "idConversacion", request.getIdConversacion()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el análisis de sentimiento: " + e.getMessage()));
        }
    }
    
    /**
     * ENDPOINT EXISTENTE: Mantiene compatibilidad con el flujo anterior
     */
    @PostMapping
    public ResponseEntity<String> analizarMensaje(@RequestBody MensajeDTO mensajeDTO) {
        logger.info("Recibida solicitud para analizar mensaje para conversación: {}", 
                   mensajeDTO.getId_conversacion());
        sentimentAnalysisService.analizarMensaje(mensajeDTO);
        return ResponseEntity.ok("Análisis de sentimiento iniciado correctamente");
    }
    
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
}