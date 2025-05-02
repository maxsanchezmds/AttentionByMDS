package com.attention.analysis.message_receiver.controller;

import com.attention.analysis.message_receiver.model.WhatsAppMessage;
import com.attention.analysis.message_receiver.service.MessageProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MessageController {

    @Autowired
    private MessageProcessorService messageProcessorService;

    @PostMapping("/webhook")
    public ResponseEntity<WhatsAppMessage> receiveWhatsAppMessage(@RequestBody WhatsAppMessage message) {
        WhatsAppMessage processedMessage = messageProcessorService.processMessage(message);
        return new ResponseEntity<>(processedMessage, HttpStatus.CREATED);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getMessageCounts() {
        return ResponseEntity.ok(messageProcessorService.getMessageCountsByPriority());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WhatsAppMessage> getMessageById(@PathVariable String id) {
        return messageProcessorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}