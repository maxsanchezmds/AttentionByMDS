package com.attention.analysis.sentiment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "svg_sentiment")
@NoArgsConstructor
@AllArgsConstructor
public class SvgSentiment {
    
    @Id
    @Column(name = "id_conversacion")
    private Long idConversacion; // Cambiado a Long para consistencia
    
    @Column(name = "promedio_sentimiento")
    private Double promedioSentimiento;
    
    @Column(name = "fecha_ultimo_mensaje")
    private LocalDateTime fechaUltimoMensaje;
    
    @Column(name = "id_empresa")
    private Long idEmpresa;
}