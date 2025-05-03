package com.attention.analysis.message_receiver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "conversations")
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.LEVE; // Por defecto LEVE
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment")
    private Sentiment sentiment = Sentiment.NEUTRO; // Por defecto NEUTRO
    
    @Column(name = "quality_score")
    private Integer qualityScore; // Por defecto NULL
    
    @Column(name = "is_responded")
    private boolean isResponded = false; // Por defecto NO (false)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "actualizado")
    private Boolean actualizado = false; // Por defecto es 'false'
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();
    
    // Enumeración para la prioridad
    public enum Priority {
        URGENTE, MODERADO, LEVE
    }
    
    // Enumeración para el sentimiento
    public enum Sentiment {
        ENOJO, CALMA, FELICIDAD, NEUTRO
    }
    
    // Método para añadir un mensaje a la conversación
    public void addMessage(Message message) {
        messages.add(message);
        message.setConversation(this);
        this.updatedAt = LocalDateTime.now();
        this.actualizado = false; // Marcar como no actualizado cuando se añade un mensaje
    }
    
    // Método para marcar la conversación como actualizada
    public void marcarComoActualizada() {
        this.actualizado = true;
    }

    // Método para marcar la conversación como no actualizada
    public void marcarComoNoActualizada() {
        this.actualizado = false;
    }

    // Método para verificar si la conversación está actualizada
    public boolean estaActualizada() {
        return this.actualizado;
    }
}