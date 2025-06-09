package com.attention.analysis.sentiment_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SentimentRequest {
    
    @NotNull(message = "El ID de conversación es requerido")
    private Long idConversacion;
    
    @NotNull(message = "El mensaje de WhatsApp es requerido")
    private WhatsappMessage whatsappMessage;
}