package com.attention.analysis.message_receiver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "whatsapp_message_id", unique = true)
    private String whatsappMessageId;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "sent_at")
    private LocalDateTime timestamp;
    
    @Column(name = "agent_id")
    private String agentId;
    
    @Column(name = "is_from_customer")
    private boolean fromCustomer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore // Para evitar ciclos en la serialización JSON
    private Conversation conversation;
    
    // Constructor conveniente
    public Message(String whatsappMessageId, String messageContent, LocalDateTime timestamp, boolean fromCustomer) {
        this.whatsappMessageId = whatsappMessageId;
        this.messageContent = messageContent;
        this.timestamp = timestamp;
        this.fromCustomer = fromCustomer;
    }
}