package com.example.classification_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clasificaciones", indexes = {
    @Index(name = "idx_conversacion_fecha", columnList = "id_conversacion, fecha_clasificacion"),
    @Index(name = "idx_fecha_clasificacion", columnList = "fecha_clasificacion"),
    @Index(name = "idx_tipo_clasificacion", columnList = "clasificacion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_conversacion", nullable = false)
    private Long idConversacion;

    @Column(name = "clasificacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoClasificacion clasificacion;

    @Column(name = "fecha_clasificacion", nullable = false)
    private LocalDateTime fechaClasificacion;

    @Column(name = "respuesta_completa_gpt", columnDefinition = "TEXT")
    private String respuestaCompletaGpt;

    @Column(name = "mensajes_analizados")
    private Integer mensajesAnalizados;

    @Column(name = "dias_historial_usado")
    private Integer diasHistorialUsado;

    public enum TipoClasificacion {
        URGENTE("Requiere atención inmediata"),
        MODERADA("Requiere atención pero no es crítica"),
        LEVE("Consulta general o situación de baja prioridad");

        private final String descripcion;

        TipoClasificacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    @PrePersist
    public void prePersist() {
        if (fechaClasificacion == null) {
            fechaClasificacion = LocalDateTime.now();
        }
    }
}