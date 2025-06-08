package com.attention.analysis.Classification_Service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ClassificationRequest {
    
    @NotNull(message = "El ID de conversación es requerido")
    private Long idConversacion;
}