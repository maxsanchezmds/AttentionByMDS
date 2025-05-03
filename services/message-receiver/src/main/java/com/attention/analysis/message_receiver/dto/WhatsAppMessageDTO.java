package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppMessageDTO {
    private String whatsappMessageId;
    private String phoneNumber;
    private String customerName;
    private String messageContent;
    private String timestamp;
    
    @JsonProperty("isFromCustomer")
    private boolean fromCustomer;
}