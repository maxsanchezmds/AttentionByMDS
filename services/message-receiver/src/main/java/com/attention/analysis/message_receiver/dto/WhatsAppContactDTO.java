package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppContactDTO {
    private WhatsAppProfileDTO profile;
    
    @JsonProperty("wa_id")
    private String waId;
}