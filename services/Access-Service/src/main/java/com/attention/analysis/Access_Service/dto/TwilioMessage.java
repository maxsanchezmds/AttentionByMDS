package com.attention.analysis.Access_Service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwilioMessage {
    
    @JsonProperty("SmsMessageSid")
    private String smsMessageSid;
    
    @JsonProperty("NumMedia")
    private String numMedia;
    
    @JsonProperty("ProfileName")
    private String profileName;
    
    @JsonProperty("MessageType")
    private String messageType;
    
    @JsonProperty("SmsSid")
    private String smsSid;
    
    @JsonProperty("WaId")
    private String waId;
    
    @JsonProperty("SmsStatus")
    private String smsStatus;
    
    @JsonProperty("Body")
    private String body;
    
    @JsonProperty("To")
    private String to;
    
    @JsonProperty("MessagingServiceSid")
    private String messagingServiceSid;
    
    @JsonProperty("NumSegments")
    private String numSegments;
    
    @JsonProperty("ReferralNumMedia")
    private String referralNumMedia;
    
    @JsonProperty("MessageSid")
    private String messageSid;
    
    @JsonProperty("AccountSid")
    private String accountSid;
    
    @JsonProperty("From")
    private String from;
    
    @JsonProperty("ApiVersion")
    private String apiVersion;
}