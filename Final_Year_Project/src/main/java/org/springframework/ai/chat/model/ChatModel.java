package org.springframework.ai.chat.model;

/**
 * Minimal stub of ChatModel used when the Spring AI dependency is not available.
 * This stub enables local compilation and returns a lightweight placeholder when called.
 */
public final class ChatModel {
    private final String name;

    public ChatModel() { this.name = "stub-model"; }

    public ChatModel(String name) { this.name = name; }

    public String getName() { return name; }
}
