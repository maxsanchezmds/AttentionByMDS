package com.attention.analysis.sentiment_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensajeDTO {
    private String contenido_mensaje;
    private String id_conversacion;
    private Long id_empresa;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha_envio;
}