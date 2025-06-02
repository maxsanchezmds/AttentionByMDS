package com.attention.analysis.Access_Service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accesos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @Column(name = "sentiment_access", nullable = false)
    private Boolean sentimentAccess = false;

    @Column(name = "classification_access", nullable = false)
    private Boolean classificationAccess = false;

    @Column(name = "attention_quality_access", nullable = false)
    private Boolean attentionQualityAccess = false;

    @Column(name = "feedback_access", nullable = false)
    private Boolean feedbackAccess = false;

    @Column(name = "learn_access", nullable = false)
    private Boolean learnAccess = false;
}