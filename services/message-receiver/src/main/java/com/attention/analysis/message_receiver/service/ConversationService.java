package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.model.Conversation;
import com.attention.analysis.message_receiver.model.Message;
import com.attention.analysis.message_receiver.repository.ConversationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    /**
     * Busca una conversación por número de teléfono o crea una nueva si no existe
     */
    @Transactional
    public Conversation findOrCreateConversation(String phoneNumber, String customerName) {
        Optional<Conversation> existingConversation = conversationRepository.findByPhoneNumber(phoneNumber);
        
        if (existingConversation.isPresent()) {
            logger.info("Encontrada conversación existente para el número: {}", phoneNumber);
            return existingConversation.get();
        } else {
            logger.info("Creando nueva conversación para el número: {}", phoneNumber);
            Conversation newConversation = new Conversation();
            newConversation.setPhoneNumber(phoneNumber);
            newConversation.setCustomerName(customerName);
            // Los valores por defecto ya están establecidos en la clase Conversation
            return conversationRepository.save(newConversation);
        }
    }
    
    /**
     * Obtiene todas las conversaciones
     */
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }
    
    /**
     * Busca una conversación por ID
     */
    public Optional<Conversation> getConversationById(String id) {
        return conversationRepository.findById(id);
    }
    
    /**
     * Añade un mensaje a una conversación y actualiza la conversación
     */
    @Transactional
    public Conversation addMessageToConversation(Conversation conversation, Message message) {
        conversation.addMessage(message);
        return conversationRepository.save(conversation);
    }
}