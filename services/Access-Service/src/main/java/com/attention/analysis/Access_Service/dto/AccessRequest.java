package com.attention.analysis.Access_Service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import com.attention.analysis.Access_Service.dto.TwilioMessage;

@Data
public class AccessRequest {
    @NotNull(message = "El ID de conversación es requerido")
    private Long idConversacion;
    
    @NotNull(message = "El mensaje de WhatsApp es requerido")
    private TwilioMessage whatsappMessage;
}