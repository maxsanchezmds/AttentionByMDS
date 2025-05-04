package com.attention.analysis.message_receiver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsAppMetadataDTO {
    @JsonProperty("display_phone_number")
    private String displayPhoneNumber;
    
    @JsonProperty("phone_number_id")
    private String phoneNumberId;
}