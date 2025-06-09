package com.attention.analysis.sentiment_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class WhatsappMessage {
    private String field;
    private Value value;

    @Data
    public static class Value {
        private String messaging_product;
        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
    }

    @Data
    public static class Metadata {
        private String display_phone_number;
        private String phone_number_id;
    }

    @Data
    public static class Contact {
        private Profile profile;
        private String wa_id;
    }

    @Data
    public static class Profile {
        private String name;
    }

    @Data
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;
        private Text text;
    }

    @Data
    public static class Text {
        private String body;
    }
}