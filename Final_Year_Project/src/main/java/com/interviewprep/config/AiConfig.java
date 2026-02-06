package com.interviewprep.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient configuration.
 * Spring AI auto-configures a ChatModel bean.
 * We create a ChatClient.Builder bean for dependency injection.
 */
@Configuration
public class AiConfig {

    /**
     * Creates a ChatClient.Builder from the auto-configured ChatModel.
     * This builder can be injected into services.
     */
    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
