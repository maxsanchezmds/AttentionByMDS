package com.attention.analysis.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistroEmpresaDTO {
    
    @NotBlank(message = "El nombre legal de la empresa es obligatorio")
    private String nombreLegalEmpresa;
    
    @NotBlank(message = "El NIT de la empresa es obligatorio")
    private String nitEmpresa;
    
    private String telefonoEmpresa;
    
    @Email(message = "Debe proporcionar un correo electrónico válido para la empresa")
    private String correoEmpresa;
    
    private String direccionEmpresa;
    
    private String paisEmpresa;
    
    private String telefonoWhatsapp;
    
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;
    
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String correoElectronico;
    
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;
    
    @NotBlank(message = "El documento de identidad es obligatorio")
    private String documentoIdentidad;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}