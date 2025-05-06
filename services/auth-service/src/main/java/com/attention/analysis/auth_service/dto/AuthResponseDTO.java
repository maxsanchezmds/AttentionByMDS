// src/main/java/com/attention/analysis/auth_service/dto/AuthResponseDTO.java
package com.attention.analysis.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String mensaje;
    private String identificadorEmpresa;
}