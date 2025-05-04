package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppMessageEntryDTO {
    private String from;
    private String id;
    private String timestamp;
    private String type;
    private WhatsAppTextDTO text;
}