package com.attention.analysis.message_receiver.repository;

import com.attention.analysis.message_receiver.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    // Buscar una conversación por número de teléfono
    Optional<Conversation> findByPhoneNumber(String phoneNumber);
    
    // Verificar si existe una conversación con el número de teléfono
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Buscar todas las conversaciones no actualizadas
    List<Conversation> findByActualizadoFalse();
}