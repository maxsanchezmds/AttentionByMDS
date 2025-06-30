package com.attention.analysis.sentiment_service.controller;

import com.attention.analysis.sentiment_service.dto.MensajeDTO;
import com.attention.analysis.sentiment_service.dto.SentimentRequest;
import com.attention.analysis.sentiment_service.model.Sentiment;
import com.attention.analysis.sentiment_service.model.AvgSentiment;
import com.attention.analysis.sentiment_service.repository.SentimentRepository;
import com.attention.analysis.sentiment_service.repository.AvgSentimentRepository;
import com.attention.analysis.sentiment_service.repository.AvgSentimentRepository;
import com.attention.analysis.sentiment_service.service.SentimentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);

    private final SentimentService sentimentService;
    private final SentimentRepository sentimentRepository;
    private final AvgSentimentRepository avgSentimentRepository;
    
    public SentimentController(SentimentService sentimentService,
                             SentimentRepository sentimentRepository,
                             AvgSentimentRepository avgSentimentRepository) {
        this.sentimentService = sentimentService;
        this.sentimentRepository = sentimentRepository;
        this.avgSentimentRepository = avgSentimentRepository;
    }

    private EntityModel<AvgSentiment> toModel(AvgSentiment avg) {
        return EntityModel.of(avg,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SentimentController.class)
                        .obtenerAnalisisPorConversacion(avg.getIdConversacion())).withSelfRel());
    }

    private EntityModel<Sentiment> toModel(Sentiment sentiment) {
        return EntityModel.of(sentiment,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SentimentController.class)
                        .obtenerAnalisisPorConversacion(sentiment.getIdConversacion())).withRel("conversacion"));
    }
    /**
     * ENDPOINT PRINCIPAL: Recibe el mismo JSON que access-service envía a classification-service
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
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error interno: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el análisis de sentimiento: " + e.getMessage()));
        }
    }
    
    /**
     * ENDPOINT DE COMPATIBILIDAD: Para análisis individual de mensajes
     */
    @PostMapping("/analizar")
    public ResponseEntity<String> analizarMensaje(@RequestBody MensajeDTO mensajeDTO) {
        try {
            logger.info("Recibida solicitud para analizar mensaje para conversación: {}", 
                       mensajeDTO.getConversacion().getId());
            sentimentService.analizarMensaje(mensajeDTO);
            return ResponseEntity.ok("Análisis de sentimiento iniciado correctamente");
        } catch (Exception e) {
            logger.error("Error al analizar mensaje: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el análisis: " + e.getMessage());
        }
    }
    
    /**
     * Obtener análisis de sentimiento por conversación
     */
    @GetMapping("/analisis/{idConversacion}")
    public ResponseEntity<?> obtenerAnalisisPorConversacion(@PathVariable Long idConversacion) {
        logger.info("Solicitando análisis para conversación: {}", idConversacion);
        
        try {
            // Buscar el promedio en avg_sentiment
            Optional<AvgSentiment> avgSentiment = avgSentimentRepository.findByIdConversacion(idConversacion);

            if (avgSentiment.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "tipo", "promedio",
                    "data", toModel(avgSentiment.get())
                ));
            } else {
                // Buscar análisis individuales
                List<Sentiment> sentimientos = sentimentRepository.findLastMessagesByConversationId(
                    idConversacion, PageRequest.of(0, 10));
                    
                if (!sentimientos.isEmpty()) {
                    List<EntityModel<Sentiment>> models = sentimientos.stream()
                            .map(this::toModel)
                            .toList();
                    CollectionModel<EntityModel<Sentiment>> collection = CollectionModel.of(models,
                            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SentimentController.class)
                                    .obtenerAnalisisPorConversacion(idConversacion)).withSelfRel());
                    return ResponseEntity.ok(Map.of(
                        "tipo", "individual",
                        "data", collection
                    ));
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener análisis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el análisis"));
        }
    }
    
    /**
     * Endpoint de salud
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "sentiment-service"
        ));
    }
}