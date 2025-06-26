package com.example.classification_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensajeDTO {
    private Long id;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean esDeEjecutivo;
    private String nombreEjecutivo;
    private ConversacionDTO conversacion;
    
    @Data
    public static class ConversacionDTO {
        private Long id;
        private LocalDateTime fechaCreacion;
        private LocalDateTime fechaActualizacion;
        private String telefonoCliente;
        private String correoEmpresa;
        private Long idEmpresa;
    }
}