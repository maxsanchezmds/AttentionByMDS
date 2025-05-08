package com.attention.analysis.Message_Receptionist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "telefono_cliente", nullable = false, unique = true)
    private String telefonoCliente;

    @Column(name = "correo_empresa", nullable = false)
    private String correoEmpresa;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;
}