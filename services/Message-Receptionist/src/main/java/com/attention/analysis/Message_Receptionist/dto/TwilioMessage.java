package com.attention.analysis.Message_Receptionist.dto;

import lombok.Data;

@Data
public class TwilioMessage {
    private String smsMessageSid;
    private String numMedia;
    private String profileName;
    private String messageType;
    private String smsSid;
    private String waId;
    private String smsStatus;
    private String body;
    private String to;
    private String messagingServiceSid;
    private String numSegments;
    private String referralNumMedia;
    private String messageSid;
    private String accountSid;
    private String from;
    private String apiVersion;
}