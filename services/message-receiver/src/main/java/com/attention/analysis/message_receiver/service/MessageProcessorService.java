package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.dto.WhatsAppMessageDTO;
import com.attention.analysis.message_receiver.model.Conversation;
import com.attention.analysis.message_receiver.model.Message;
import com.attention.analysis.message_receiver.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class MessageProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorService.class);

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ConversationService conversationService;

    /**
     * Procesa un mensaje entrante de WhatsApp
     */
    @Transactional
    public Message processMessage(WhatsAppMessageDTO messageDTO) {
        logger.info("Procesando mensaje de WhatsApp: {}", messageDTO.getWhatsappMessageId());
        
        // 1. Buscar o crear la conversación para este número de teléfono
        Conversation conversation = conversationService.findOrCreateConversation(
            messageDTO.getPhoneNumber(), 
            messageDTO.getCustomerName()
        );
        
        // 2. Convertir timestamp de String a LocalDateTime
        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(messageDTO.getTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            logger.warn("Error al parsear fecha: {}, usando fecha actual", e.getMessage());
            timestamp = LocalDateTime.now();
        }
        
        // 3. Crear una nueva entidad Message
        Message message = new Message(
            messageDTO.getWhatsappMessageId(),
            messageDTO.getMessageContent(),
            timestamp,
            messageDTO.isFromCustomer()
        );
        
        // 4. Añadir el mensaje a la conversación
        conversation = conversationService.addMessageToConversation(conversation, message);
        
        logger.info("Mensaje procesado correctamente: {}", messageDTO.getWhatsappMessageId());
        
        return message;
    }
    
    /**
     * Obtiene un mensaje por su ID
     */
    public Optional<Message> getMessageById(String id) {
        return messageRepository.findById(id);
    }
    
    /**
     * Obtiene todos los mensajes de una conversación
     */
    public List<Message> getMessagesByConversation(String conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }
}