package com.attention.analysis.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmpresaWhatsappDTO {
    private Long id;
    private String correoEmpresa;
    private String telefonoWhatsapp;
}