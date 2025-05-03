package com.attention.analysis.message_receiver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Añade esta línea
public class WhatsAppMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "whatsapp_message_id", unique = true)
    private String whatsappMessageId;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "sent_at")
    private String timestamp;  // Ya cambiado a String como sugerí anteriormente
    
    @Column(name = "agent_id")
    private String agentId;
    
    @Column(name = "is_from_customer")
    @JsonProperty("isFromCustomer")  // Añade esta línea
    private boolean fromCustomer;    // Cambia el nombre del campo
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private MessagePriority priority;
    
    public enum MessageStatus {
        RECEIVED, REPLIED, PENDING
    }
    
    public enum MessagePriority {
        URGENTE, MODERADO, LEVE
    }
}