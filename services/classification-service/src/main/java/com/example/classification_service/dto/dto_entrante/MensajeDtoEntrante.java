package com.example.classification_service.dto.dto_entrante;

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
import jakarta.persistence.Lob; // Para textos que pueden superar los 255 caracteres


@Data // Genera getters y setters automáticamente
@Builder // Permite crear objetos de forma más sencilla y menos propensa a errores
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class MensajeDtoEntrante {
    @NotNull(message = "Favor indicar fecha del la solicitud")
    private LocalDateTime fecha;

    @NotNull(message = "Favor indicar la idEmpresa")
    private Long idEmpresa;

    @NotNull(message = "Favor indicar la idCliente")
    private Long idCliente;

    @NotNull(message = "Favor inidicar la idConversacion")
    private Long idConversacion;

    @NotBlank(message = "No hay contenido en el mensaje")
    private String conversacion;
}
