package com.attention.analysis.Message_Receptionist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @ManyToOne
    @JoinColumn(name = "id_conversacion", nullable = false)
    private Conversacion conversacion;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @Column(name = "es_de_ejecutivo", nullable = false)
    private boolean esDeEjecutivo = false;
    
    @Column(name = "nombre_ejecutivo")
    private String nombreEjecutivo;
}