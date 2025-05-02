package com.attention.analysis.message_receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ContextRegionProviderAutoConfiguration.class})
@EnableAsync
public class MessageReceiverApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageReceiverApplication.class, args);
    }
}
