package com.example.classification_service.service;

import com.example.classification_service.dto.ClassificationRequest;
import com.example.classification_service.dto.MensajeDTO;
import com.example.classification_service.model.Clasificacion;
import com.example.classification_service.repository.ClasificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassificationService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int DIAS_HISTORIAL = 10;
    
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
        
        // Obtener todos los mensajes de la conversación
        List<MensajeDTO> todosMensajes = messageReceptionistClient.obtenerMensajesConversacion(idConversacion);
        
        if (todosMensajes == null || todosMensajes.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron mensajes para la conversación " + idConversacion);
        }
        
        // Filtrar mensajes de los últimos 10 días (excluyendo días sin mensajes)
        List<MensajeDTO> mensajesRecientes = filtrarMensajesUltimosDias(todosMensajes);
        
        if (mensajesRecientes.isEmpty()) {
            logger.warn("No se encontraron mensajes en los últimos {} días para conversación {}", 
                       DIAS_HISTORIAL, idConversacion);
            // Si no hay mensajes recientes, usar todos los mensajes disponibles
            mensajesRecientes = todosMensajes;
        }
        
        logger.info("Usando {} mensajes de los últimos {} días para clasificación", 
                   mensajesRecientes.size(), DIAS_HISTORIAL);
        
        // Formatear la conversación
        String conversacionFormateada = formatearConversacion(mensajesRecientes);
        logger.debug("Conversación formateada:\n{}", conversacionFormateada);
        
        // Clasificar con OpenAI
        String clasificacionStr = openAIService.clasificarConversacion(conversacionFormateada);
        
        // Validar y convertir la clasificación
        Clasificacion.TipoClasificacion tipoClasificacion = convertirClasificacion(clasificacionStr);
        
        // Buscar clasificación existente o crear nueva
        Optional<Clasificacion> clasificacionExistente = clasificacionRepository.findByIdConversacion(idConversacion);
        
        Clasificacion clasificacion;
        if (clasificacionExistente.isPresent()) {
            // Actualizar clasificación existente
            clasificacion = clasificacionExistente.get();
            logger.info("Actualizando clasificación existente para conversación {}", idConversacion);
        } else {
            // Crear nueva clasificación
            clasificacion = new Clasificacion();
            clasificacion.setIdConversacion(idConversacion);
            logger.info("Creando nueva clasificación para conversación {}", idConversacion);
        }
        
        // Actualizar los valores
        clasificacion.setClasificacion(tipoClasificacion);
        clasificacion.setFechaClasificacion(LocalDateTime.now());
        clasificacion.setRespuestaCompletaGpt(clasificacionStr);
        clasificacion.setMensajesAnalizados(mensajesRecientes.size());
        clasificacion.setDiasHistorialUsado(DIAS_HISTORIAL);
        
        Clasificacion clasificacionGuardada = clasificacionRepository.save(clasificacion);
        logger.info("Clasificación actualizada: Conversación {} - {} (basada en {} mensajes)", 
                   idConversacion, tipoClasificacion, mensajesRecientes.size());
        
        return clasificacionGuardada;
    }
    
    /**
     * Filtra los mensajes para obtener solo aquellos de los últimos 10 días
     * que realmente contienen mensajes (excluyendo días sin actividad)
     */
    private List<MensajeDTO> filtrarMensajesUltimosDias(List<MensajeDTO> mensajes) {
        LocalDate fechaLimite = LocalDate.now().minusDays(DIAS_HISTORIAL);
        
        // Obtener días únicos con mensajes, ordenados de más reciente a más antiguo
        List<LocalDate> diasConMensajes = mensajes.stream()
                .map(mensaje -> mensaje.getFecha().toLocalDate())
                .distinct()
                .sorted((fecha1, fecha2) -> fecha2.compareTo(fecha1)) // Orden descendente
                .collect(Collectors.toList());
        
        logger.debug("Días con mensajes encontrados: {}", diasConMensajes.size());
        
        // Tomar solo los primeros 10 días con mensajes
        List<LocalDate> diasSeleccionados = diasConMensajes.stream()
                .limit(DIAS_HISTORIAL)
                .collect(Collectors.toList());
        
        if (!diasSeleccionados.isEmpty()) {
            LocalDate diaSeleccionadoMasAntiguo = diasSeleccionados.get(diasSeleccionados.size() - 1);
            logger.info("Filtrando mensajes desde {} (últimos {} días con actividad)", 
                       diaSeleccionadoMasAntiguo, diasSeleccionados.size());
        }
        
        // Filtrar mensajes que estén en los días seleccionados
        return mensajes.stream()
                .filter(mensaje -> {
                    LocalDate fechaMensaje = mensaje.getFecha().toLocalDate();
                    return diasSeleccionados.contains(fechaMensaje);
                })
                .sorted((m1, m2) -> m1.getFecha().compareTo(m2.getFecha())) // Orden cronológico
                .collect(Collectors.toList());
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
    
    /**
     * Método para obtener estadísticas de la clasificación
     */
    public String obtenerEstadisticasClasificacion(Long idConversacion) {
        try {
            List<MensajeDTO> todosMensajes = messageReceptionistClient.obtenerMensajesConversacion(idConversacion);
            List<MensajeDTO> mensajesRecientes = filtrarMensajesUltimosDias(todosMensajes);
            
            return String.format("Conversación %d: %d mensajes totales, %d mensajes en últimos %d días", 
                               idConversacion, todosMensajes.size(), mensajesRecientes.size(), DIAS_HISTORIAL);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas: {}", e.getMessage());
            return "Error al obtener estadísticas";
        }
    }
}