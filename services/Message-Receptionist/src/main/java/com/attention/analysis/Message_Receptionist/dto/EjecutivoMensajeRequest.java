package com.attention.analysis.Message_Receptionist.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class EjecutivoMensajeRequest {
    
    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;
    
    @NotBlank(message = "El número de teléfono de la empresa es requerido")
    private String numeroTelefonoEmpresa;
    
    @NotBlank(message = "El nombre del ejecutivo es requerido")
    private String nombreCompletoEjecutivo;
    
    @NotBlank(message = "El número del cliente es requerido")
    private String numeroTelefonoCliente;
}