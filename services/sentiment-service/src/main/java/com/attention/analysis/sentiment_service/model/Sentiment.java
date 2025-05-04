package com.attention.analysis.sentiment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sentiment")
@NoArgsConstructor
@AllArgsConstructor
public class Sentiment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_empresa")
    private Long idEmpresa;
    
    @Column(name = "id_conversacion")
    private String idConversacion;
    
    @Column(name = "contenido_mensaje", columnDefinition = "TEXT")
    private String contenidoMensaje;
    
    @Column(name = "sentimiento")
    private Integer sentimiento;
    
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;
}