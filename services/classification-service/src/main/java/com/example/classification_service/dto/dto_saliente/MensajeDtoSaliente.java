package com.example.classification_service.dto.dto_saliente;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data // Genera getters y setters automáticamente
@Builder // Permite crear objetos de forma más sencilla y menos propensa a errores
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class MensajeDtoSaliente {
    private Long id;
    private LocalDateTime fechaClasificacion;
    private Long idEmpresa;
    private Long idCliente;
    private Long idConversacion;
    private String clasificacion; // "Urgente", "Moderada", "Leve"
    private String descripcion; // por qué se clasifica como urgente, moderada o leve
}
