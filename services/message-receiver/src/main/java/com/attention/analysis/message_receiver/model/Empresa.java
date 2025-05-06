package com.attention.analysis.message_receiver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "empresas")
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEmpresa;
    
    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;
    
    @Column(name = "identificador_empresa", nullable = false, unique = true)
    private String identificadorEmpresa;
    
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conversation> conversations = new ArrayList<>();
    
    // Método para generar un identificador si no tiene uno
    public void setIdentificadorEmpresa(String identificadorEmpresa) {
        this.identificadorEmpresa = identificadorEmpresa;
    }
}