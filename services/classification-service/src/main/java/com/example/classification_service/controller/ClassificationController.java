package com.example.classification_service.controller;

import com.attention.analysis.Classification_Service.dto.ClassificationRequest;
import com.attention.analysis.Classification_Service.model.Clasificacion;
import com.attention.analysis.Classification_Service.repository.ClasificacionRepository;
import com.attention.analysis.Classification_Service.service.ClassificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {
    
    private final ClassificationService classificationService;
    private final ClasificacionRepository clasificacionRepository;
    
    public ClassificationController(ClassificationService classificationService,
                                   ClasificacionRepository clasificacionRepository) {
        this.classificationService = classificationService;
        this.clasificacionRepository = clasificacionRepository;
    }
    
    @PostMapping("/classify")
    public ResponseEntity<?> clasificarConversacion(@Valid @RequestBody ClassificationRequest request) {
        try {
            Clasificacion clasificacion = classificationService.procesarClasificacion(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("clasificacion", clasificacion);
            response.put("mensaje", "Clasificación procesada exitosamente");
            response.put("reclasificacion", clasificacionRepository.countByIdConversacion(request.getIdConversacion()) > 1);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la clasificación: " + e.getMessage()));
        }
    }
    
    @GetMapping("/conversacion/{idConversacion}")
    public ResponseEntity<?> obtenerClasificacionActual(@PathVariable Long idConversacion) {
        try {
            Clasificacion clasificacion = classificationService.obtenerClasificacion(idConversacion);
            return ResponseEntity.ok(clasificacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener la clasificación"));
        }
    }
    
    @GetMapping("/conversacion/{idConversacion}/historial")
    public ResponseEntity<?> obtenerHistorialClasificaciones(@PathVariable Long idConversacion) {
        try {
            List<Clasificacion> historial = clasificacionRepository
                    .findAllByIdConversacionOrderByFechaDesc(idConversacion);
            
            if (historial.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("idConversacion", idConversacion);
            response.put("totalClasificaciones", historial.size());
            response.put("historial", historial);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el historial"));
        }
    }
    
    @GetMapping("/conversacion/{idConversacion}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable Long idConversacion) {
        try {
            String estadisticas = classificationService.obtenerEstadisticasClasificacion(idConversacion);
            
            Map<String, Object> response = new HashMap<>();
            response.put("idConversacion", idConversacion);
            response.put("estadisticas", estadisticas);
            response.put("totalClasificaciones", clasificacionRepository.countByIdConversacion(idConversacion));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener estadísticas"));
        }
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> obtenerClasificacionesPorTipo(@PathVariable String tipo) {
        try {
            Clasificacion.TipoClasificacion tipoClasificacion = 
                    Clasificacion.TipoClasificacion.valueOf(tipo.toUpperCase());
            
            List<Clasificacion> clasificaciones = clasificacionRepository
                    .findByTipoClasificacion(tipoClasificacion);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tipo", tipoClasificacion);
            response.put("descripcion", tipoClasificacion.getDescripcion());
            response.put("total", clasificaciones.size());
            response.put("clasificaciones", clasificaciones);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tipo de clasificación no válido: " + tipo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener clasificaciones por tipo"));
        }
    }
    
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen() {
        try {
            LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
            
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("urgentes_24h", clasificacionRepository
                    .countByTipoAndFechaDesde(Clasificacion.TipoClasificacion.URGENTE, hace24Horas));
            resumen.put("moderadas_24h", clasificacionRepository
                    .countByTipoAndFechaDesde(Clasificacion.TipoClasificacion.MODERADA, hace24Horas));
            resumen.put("leves_24h", clasificacionRepository
                    .countByTipoAndFechaDesde(Clasificacion.TipoClasificacion.LEVE, hace24Horas));
            resumen.put("total_clasificaciones", clasificacionRepository.count());
            resumen.put("fecha_consulta", LocalDateTime.now());
            
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener resumen"));
        }
    }
}