package com.attention.analysis.message_receiver.controller;

import com.attention.analysis.message_receiver.dto.WhatsAppMessageDTO;
import com.attention.analysis.message_receiver.dto.WhatsAppWebhookDTO;
import com.attention.analysis.message_receiver.model.Conversation;
import com.attention.analysis.message_receiver.model.Message;
import com.attention.analysis.message_receiver.service.ConversationService;
import com.attention.analysis.message_receiver.service.MessageProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/")
public class MessageController {

    @Autowired
    private MessageProcessorService messageProcessorService;
    
    @Autowired
    private ConversationService conversationService;

    /**
     * Endpoint para recibir webhooks de WhatsApp
     */
    @PostMapping("/webhook")
    public ResponseEntity<Message> receiveWhatsAppWebhook(@RequestBody WhatsAppWebhookDTO webhookDTO) {
        Message processedMessage = messageProcessorService.processWebhook(webhookDTO);
        return new ResponseEntity<>(processedMessage, HttpStatus.CREATED);
    }
    
    /**
     * Endpoint para recibir mensajes de WhatsApp (formato antiguo)
     * Mantenido por compatibilidad
     */
    @PostMapping("/webhook/legacy")
    public ResponseEntity<Message> receiveWhatsAppMessage(@RequestBody WhatsAppMessageDTO messageDTO) {
        Message processedMessage = messageProcessorService.processMessage(messageDTO);
        return new ResponseEntity<>(processedMessage, HttpStatus.CREATED);
    }
    
    /**
     * Obtener todas las conversaciones
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }
    
    /**
     * Obtener una conversación por ID
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable String id) {
        return conversationService.getConversationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Obtener mensajes de una conversación
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<Message>> getMessagesByConversation(@PathVariable String id) {
        return ResponseEntity.ok(messageProcessorService.getMessagesByConversation(id));
    }
    
    /**
     * Obtener un mensaje por ID
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable String id) {
        return messageProcessorService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}