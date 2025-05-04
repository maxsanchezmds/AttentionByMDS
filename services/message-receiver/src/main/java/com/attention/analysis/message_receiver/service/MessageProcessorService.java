package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.dto.WhatsAppMessageDTO;
import com.attention.analysis.message_receiver.dto.WhatsAppWebhookDTO;
import com.attention.analysis.message_receiver.dto.WhatsAppMessageEntryDTO;
import com.attention.analysis.message_receiver.dto.WhatsAppContactDTO;
import com.attention.analysis.message_receiver.model.Conversation;
import com.attention.analysis.message_receiver.model.Empresa;
import com.attention.analysis.message_receiver.model.Message;
import com.attention.analysis.message_receiver.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    /**
     * Procesa un webhook de WhatsApp (nuevo formato)
     */
    @Transactional
    public Message processWebhook(WhatsAppWebhookDTO webhookDTO) {
        logger.info("Procesando webhook de WhatsApp: {}", webhookDTO.getField());
        
        // Validar que el webhook contiene mensajes
        if (webhookDTO.getValue() == null || 
            webhookDTO.getValue().getMessages() == null || 
            webhookDTO.getValue().getMessages().isEmpty() ||
            webhookDTO.getValue().getContacts() == null ||
            webhookDTO.getValue().getContacts().isEmpty()) {
            
            logger.warn("Webhook recibido sin mensajes o contactos válidos");
            throw new RuntimeException("Webhook no contiene mensajes o contactos válidos");
        }
        
        // Obtener el primer mensaje y contacto
        WhatsAppMessageEntryDTO messageEntry = webhookDTO.getValue().getMessages().get(0);
        WhatsAppContactDTO contact = webhookDTO.getValue().getContacts().get(0);
        
        // Validar que el mensaje es de tipo texto
        if (!"text".equals(messageEntry.getType()) || messageEntry.getText() == null) {
            logger.warn("Mensaje recibido no es de tipo texto: {}", messageEntry.getType());
            throw new RuntimeException("Solo se soportan mensajes de tipo texto");
        }
        
        // Extraer datos necesarios
        String whatsappMessageId = messageEntry.getId();
        String phoneNumber = contact.getWaId();
        String customerName = contact.getProfile() != null ? contact.getProfile().getName() : "Unknown";
        String messageContent = messageEntry.getText().getBody();
        
        // Convertir Unix timestamp a LocalDateTime
        LocalDateTime timestamp;
        try {
            long unixTimestamp = Long.parseLong(messageEntry.getTimestamp());
            timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
        } catch (Exception e) {
            logger.warn("Error al parsear timestamp: {}, usando fecha actual", e.getMessage());
            timestamp = LocalDateTime.now();
        }
        
        // Todos los mensajes recibidos de la API de WhatsApp son del cliente
        boolean isFromCustomer = true;
        
        // Obtener o crear la empresa (aquí usamos un nombre predeterminado, podría venir de los datos)
        Empresa empresa = empresaService.findOrCreateEmpresa("DefaultCompany");
        
        // Buscar o crear la conversación, ahora incluyendo la empresa
        Conversation conversation = conversationService.findOrCreateConversation(phoneNumber, customerName, empresa);
        
        // Crear una nueva entidad Message
        Message message = new Message(
            whatsappMessageId,
            messageContent,
            timestamp,
            isFromCustomer
        );
        
        // Añadir el mensaje a la conversación
        conversation = conversationService.addMessageToConversation(conversation, message);
        
        // Enviar el mensaje para análisis de sentimiento
        sentimentAnalysisService.enviarMensajeParaAnalisis(message);
        
        logger.info("Mensaje procesado correctamente: {}", whatsappMessageId);
        
        return message;
    }
    
    /**
     * Procesa un mensaje entrante de WhatsApp (formato antiguo)
     */
    @Transactional
    public Message processMessage(WhatsAppMessageDTO messageDTO) {
        logger.info("Procesando mensaje de WhatsApp: {}", messageDTO.getWhatsappMessageId());
        
        // Obtener o crear la empresa (usando un nombre predeterminado)
        Empresa empresa = empresaService.findOrCreateEmpresa("DefaultCompany");
        
        // 1. Buscar o crear la conversación para este número de teléfono, ahora incluyendo la empresa
        Conversation conversation = conversationService.findOrCreateConversation(
            messageDTO.getPhoneNumber(), 
            messageDTO.getCustomerName(),
            empresa
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
        
        // 5. Enviar el mensaje para análisis de sentimiento
        sentimentAnalysisService.enviarMensajeParaAnalisis(message);
        
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