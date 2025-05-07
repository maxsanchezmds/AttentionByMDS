package com.attention.analysis.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empresas")
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;
    
    @Column(name = "nombre_empresa", nullable = false)
    private String nombreLegal;
    
    @Column(name = "identificador_empresa", nullable = false, unique = true)
    private String identificadorEmpresa;
    
    @Column(name = "telefono_empresa")
    private String telefonoEmpresa;
    
    @Column(name = "correo_empresa")
    private String correoEmpresa;
    
    @Column(name = "nit_empresa", unique = true)
    private String nitEmpresa;
    
    @Column(name = "direccion_empresa")
    private String direccionEmpresa;
    
    @Column(name = "pais_empresa")
    private String paisEmpresa;
    
    @Column(name = "telefono_whatsapp")
    private String telefonoWhatsapp;
    
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Usuario> usuarios = new ArrayList<>();
    
    @PrePersist
    public void generarIdentificador() {
        if (identificadorEmpresa == null) {
            identificadorEmpresa = UUID.randomUUID().toString();
        }
    }
}