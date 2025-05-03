package com.attention.analysis.message_receiver.repository;

import com.attention.analysis.message_receiver.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    // Buscar mensaje por ID de WhatsApp
    Message findByWhatsappMessageId(String whatsappMessageId);
    
    // Buscar todos los mensajes de una conversación
    List<Message> findByConversationId(String conversationId);
}