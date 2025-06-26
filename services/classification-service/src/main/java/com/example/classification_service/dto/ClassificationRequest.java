package com.example.classification_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ClassificationRequest {
    
    @NotNull(message = "El ID de conversación es requerido")
    private Long idConversacion;
}