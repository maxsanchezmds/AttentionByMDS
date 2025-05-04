package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppWebhookDTO {
    private String field;
    private WhatsAppValueDTO value;
}