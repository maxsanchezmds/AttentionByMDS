package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class WhatsAppValueDTO {
    @JsonProperty("messaging_product")
    private String messagingProduct;
    
    private WhatsAppMetadataDTO metadata;
    private List<WhatsAppContactDTO> contacts;
    private List<WhatsAppMessageEntryDTO> messages;
}