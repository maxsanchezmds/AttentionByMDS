package com.attention.analysis.message_receiver.service;

import com.attention.analysis.message_receiver.model.WhatsAppMessage;
import com.attention.analysis.message_receiver.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;
import java.util.Optional;

@Service
public class MessageProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorService.class);

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private SqsClient sqsClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public WhatsAppMessage processMessage(WhatsAppMessage message) {
        logger.info("Processing message: {}", message.getWhatsappMessageId());
        
        // Si el mensaje no tiene prioridad asignada, establecerla como MODERADO por defecto
        if (message.getPriority() == null) {
            message.setPriority(WhatsAppMessage.MessagePriority.MODERADO);
        }
        
        // Si el mensaje no tiene estado asignado, establecerlo como RECEIVED por defecto
        if (message.getStatus() == null) {
            message.setStatus(WhatsAppMessage.MessageStatus.RECEIVED);
        }
        
        // 1. Guardar mensaje en la base de datos
        WhatsAppMessage savedMessage = messageRepository.save(message);
        
        try {
            // 2. Enviar mensaje a SQS para procesamiento asíncrono
            String messageBody = objectMapper.writeValueAsString(savedMessage);
            
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();
            
            sqsClient.sendMessage(sendMsgRequest);
            
            logger.info("Message processed and sent to queue: {}", message.getWhatsappMessageId());
        } catch (Exception e) {
            logger.error("Error sending message to SQS: {}", e.getMessage(), e);
        }
        
        return savedMessage;
    }
    
    public Map<String, Long> getMessageCountsByPriority() {
        return messageRepository.countByPriority();
    }
    
    public Optional<WhatsAppMessage> findById(String id) {
        return messageRepository.findById(id);
    }
}