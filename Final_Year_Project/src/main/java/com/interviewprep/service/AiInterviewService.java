package com.interviewprep.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Role-aware AI service using configured LLM provider (Grok by default). Injects user's target role into system-style prompts
 * for question generation and answer evaluation.
 */
@Service
public class AiInterviewService {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an experienced technical interviewer hiring for the role of %s. \
        Generate precise, role-appropriate questions on Java Full Stack topics. \
        Evaluate answers based on what is expected at this role level. \
        Provide professional, constructive feedback with specific suggestions relevant to this role. \
        Be concise and structured. Do not repeat the question in your response.
        """;

    private final ChatClient chatClient;

    public AiInterviewService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Generate a single interview question tailored to the role, optional topic and difficulty.
     */
    public String generateQuestion(String userRole, String mode, String topic, String difficulty) {
        String context = buildQuestionContext(mode, topic, difficulty);
        String fullPrompt = "Role: " + userRole + "\n" + context + "\nGenerate exactly one clear interview question. Output only the question, no numbering or extra text.";
        return callAi(fullPrompt, userRole);
    }

    /**
     * Generate a coding problem statement for the given role and optional topic.
     */
    public String generateCodingProblem(String userRole, String topic) {
        String topicPart = (topic != null && !topic.isBlank()) ? " Focus on: " + topic + "." : "";
        String fullPrompt = "Role: " + userRole + "." + topicPart +
                "\nGenerate one short Java coding problem (2-4 sentences). Output only the problem statement, no code.";
        return callAi(fullPrompt, userRole);
    }

    /**
     * Evaluate user's answer and return role-aware feedback (and optional score 0-100).
     */
    public String evaluateAnswer(String userRole, String question, String userAnswer, String topic, String difficulty) {
        String fullPrompt = String.format(
                "Question: %s\nUser answer: %s\nTopic: %s. Difficulty: %s.\nAs an interviewer for the role of %s, evaluate this answer. " +
                        "Give brief, constructive feedback (2-4 sentences). Start with 'Feedback:' and suggest what to improve for this role. End with 'Score: N' (0-100).",
                question, userAnswer, topic != null ? topic : "General", difficulty != null ? difficulty : "Medium", userRole);
        return callAi(fullPrompt, userRole);
    }

    /**
     * Evaluate code submission with role-aware feedback (e.g. enterprise practices for Senior role).
     */
    public String evaluateCode(String userRole, String problemStatement, String code, String executionOutput) {
        String fullPrompt = String.format(
                "Role: %s. Problem: %s\n\nUser code:\n%s\n\nExecution output: %s\n" +
                        "Evaluate the code for correctness, style, and role-relevant best practices. " +
                        "Give brief feedback (2-5 sentences). Start with 'Feedback:' and end with 'Score: N' (0-100).",
                userRole, problemStatement != null ? problemStatement : "Coding task", code,
                executionOutput != null ? executionOutput : "No output");
        return callAi(fullPrompt, userRole);
    }

    private String buildQuestionContext(String mode, String topic, String difficulty) {
        StringBuilder sb = new StringBuilder();
        if (mode != null && !mode.isBlank()) sb.append("Mode: ").append(mode).append(". ");
        if (topic != null && !topic.isBlank()) sb.append("Topic: ").append(topic).append(". ");
        if (difficulty != null && !difficulty.isBlank()) sb.append("Difficulty: ").append(difficulty).append(".");
        return sb.toString();
    }

    private String callAi(String userPrompt, String role) {
        try {
            String systemText = String.format(SYSTEM_PROMPT_TEMPLATE, role != null && !role.isBlank() ? role : "Java Developer");
            return chatClient.prompt()
                    .system(systemText)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return "AI is temporarily unavailable. Ensure your Grok API key is set (GROK_API_KEY) and backend can reach the Grok endpoint. Error: " + e.getMessage();
        }
    }
}
