package com.attention.analysis.Classification_Service.service;

import com.attention.analysis.Classification_Service.dto.ClassificationRequest;
import com.attention.analysis.Classification_Service.dto.MensajeDTO;
import com.attention.analysis.Classification_Service.model.Clasificacion;
import com.attention.analysis.Classification_Service.repository.ClasificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final ClasificacionRepository clasificacionRepository;
    private final MessageReceptionistClient messageReceptionistClient;
    private final OpenAIService openAIService;
    
    public ClassificationService(ClasificacionRepository clasificacionRepository,
                               MessageReceptionistClient messageReceptionistClient,
                               OpenAIService openAIService) {
        this.clasificacionRepository = clasificacionRepository;
        this.messageReceptionistClient = messageReceptionistClient;
        this.openAIService = openAIService;
    }
    
    @Transactional
    public Clasificacion procesarClasificacion(ClassificationRequest request) {
        Long idConversacion = request.getIdConversacion();
        logger.info("Procesando clasificación para conversación ID: {}", idConversacion);
        
        // Verificar si ya existe una clasificación para esta conversación
        if (clasificacionRepository.existsByIdConversacion(idConversacion)) {
            logger.info("Ya existe una clasificación para la conversación {}", idConversacion);
            return clasificacionRepository.findByIdConversacion(idConversacion).orElseThrow();
        }
        
        // Obtener mensajes de la conversación
        List<MensajeDTO> mensajes = messageReceptionistClient.obtenerMensajesConversacion(idConversacion);
        
        if (mensajes == null || mensajes.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron mensajes para la conversación " + idConversacion);
        }
        
        // Formatear la conversación
        String conversacionFormateada = formatearConversacion(mensajes);
        logger.debug("Conversación formateada:\n{}", conversacionFormateada);
        
        // Clasificar con OpenAI
        String clasificacionStr = openAIService.clasificarConversacion(conversacionFormateada);
        
        // Validar y convertir la clasificación
        Clasificacion.TipoClasificacion tipoClasificacion = convertirClasificacion(clasificacionStr);
        
        // Guardar la clasificación
        Clasificacion clasificacion = new Clasificacion();
        clasificacion.setIdConversacion(idConversacion);
        clasificacion.setClasificacion(tipoClasificacion);
        clasificacion.setFechaClasificacion(LocalDateTime.now());
        clasificacion.setRespuestaCompletaGpt(clasificacionStr);
        
        Clasificacion clasificacionGuardada = clasificacionRepository.save(clasificacion);
        logger.info("Clasificación guardada: Conversación {} - {}", idConversacion, tipoClasificacion);
        
        return clasificacionGuardada;
    }
    
    private String formatearConversacion(List<MensajeDTO> mensajes) {
        return mensajes.stream()
                .map(mensaje -> {
                    String emisor = mensaje.isEsDeEjecutivo() ? 
                            "Ejecutivo" + (mensaje.getNombreEjecutivo() != null ? 
                                " (" + mensaje.getNombreEjecutivo() + ")" : "") : 
                            "Cliente";
                    String fecha = mensaje.getFecha().format(FORMATTER);
                    return String.format("%s [%s]: \"%s\"", emisor, fecha, mensaje.getMensaje());
                })
                .collect(Collectors.joining("\n"));
    }
    
    private Clasificacion.TipoClasificacion convertirClasificacion(String clasificacionStr) {
        try {
            return Clasificacion.TipoClasificacion.valueOf(clasificacionStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            logger.error("Clasificación no válida recibida: {}", clasificacionStr);
            // Por defecto, asignar MODERADA si no se puede determinar
            return Clasificacion.TipoClasificacion.MODERADA;
        }
    }
    
    public Clasificacion obtenerClasificacion(Long idConversacion) {
        return clasificacionRepository.findByIdConversacion(idConversacion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró clasificación para la conversación " + idConversacion));
    }
}