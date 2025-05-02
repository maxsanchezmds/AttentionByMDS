package com.attention.analysis.message_receiver.repository;

import com.attention.analysis.message_receiver.model.WhatsAppMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Map;

@Repository
public interface MessageRepository extends JpaRepository<WhatsAppMessage, String> {
    
    WhatsAppMessage findByWhatsappMessageId(String whatsappMessageId);
    
    @Query("SELECT m.priority as priority, COUNT(m) as count FROM WhatsAppMessage m GROUP BY m.priority")
    Map<String, Long> countByPriority();
}