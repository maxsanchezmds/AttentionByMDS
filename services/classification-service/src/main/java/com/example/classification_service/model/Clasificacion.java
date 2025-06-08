package com.attention.analysis.Classification_Service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clasificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_conversacion", nullable = false, unique = true)
    private Long idConversacion;

    @Column(name = "clasificacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoClasificacion clasificacion;

    @Column(name = "fecha_clasificacion", nullable = false)
    private LocalDateTime fechaClasificacion;

    @Column(name = "respuesta_completa_gpt", columnDefinition = "TEXT")
    private String respuestaCompletaGpt;

    public enum TipoClasificacion {
        URGENTE,
        MODERADA,
        LEVE
    }
}