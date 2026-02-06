package org.springframework.ai.chat.client;

import org.springframework.ai.chat.model.ChatModel;

/**
 * Lightweight stub of ChatClient to allow local builds when Spring AI libraries are absent.
 * The stub provides a builder and a simple prompt API that returns a fixed response.
 */
public class ChatClient {

    public static Builder builder(ChatModel model) {
        return new Builder(model);
    }

    public ChatClient() { }

    public PromptSpec prompt() { return new PromptSpec(); }

    public static final class Builder {
        private final ChatModel model;
        public Builder(ChatModel model) { this.model = model; }
        public ChatClient build() { return new ChatClient(); }
    }

    public static final class PromptSpec {
        private String systemText;
        private String userText;

        public PromptSpec system(String text) { this.systemText = text; return this; }
        public PromptSpec user(String text) { this.userText = text; return this; }

        public ChatResponse call() {
            String combined = "[stub] System: " + (systemText == null ? "" : systemText)
                    + " | User: " + (userText == null ? "" : userText);
            return new ChatResponse(combined);
        }
    }

    public static final class ChatResponse {
        private final String payload;
        public ChatResponse(String payload) { this.payload = payload; }
        public String content() { return payload; }
    }
}
