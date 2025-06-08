package com.attention.analysis.Classification_Service.controller;

import com.attention.analysis.Classification_Service.dto.ClassificationRequest;
import com.attention.analysis.Classification_Service.model.Clasificacion;
import com.attention.analysis.Classification_Service.service.ClassificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/classification")
public class ClassificationController {
    
    private final ClassificationService classificationService;
    
    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }
    
    @PostMapping("/classify")
    public ResponseEntity<?> clasificarConversacion(@Valid @RequestBody ClassificationRequest request) {
        try {
            Clasificacion clasificacion = classificationService.procesarClasificacion(request);
            return ResponseEntity.ok(clasificacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la clasificación: " + e.getMessage());
        }
    }
    
    @GetMapping("/conversacion/{idConversacion}")
    public ResponseEntity<?> obtenerClasificacion(@PathVariable Long idConversacion) {
        try {
            Clasificacion clasificacion = classificationService.obtenerClasificacion(idConversacion);
            return ResponseEntity.ok(clasificacion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la clasificación");
        }
    }
}