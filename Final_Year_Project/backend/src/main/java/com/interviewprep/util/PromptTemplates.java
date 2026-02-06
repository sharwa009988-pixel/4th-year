package com.interviewprep.util;

/**
 * Centralized prompt templates for AI interview system.
 * These prompts are optimized for hosted LLMs such as Grok (grokxai).
 *
 * All templates are role-aware: the selected professional role is injected into the system
 * prompt so that questions, difficulty and feedback are tailored to that role.
 */
public class PromptTemplates {

    /**
     * Build a system prompt for the AI interviewer persona.
     * This MUST include the user's selected target role.
     */
    public static String buildSystemPrompt(String role) {
        String safeRole = (role == null || role.isBlank()) ? "Software Engineer (General)" : role.trim();
        return """
                You are a professional senior interviewer hiring for the role of %s.
                Generate one relevant, high-quality interview question at a time based on the conversation so far.
                Focus on topics, technologies, and scenarios that are realistic and appropriate for this role.

                General guidelines:
                - Ask precise, job-relevant questions appropriate for the difficulty level
                - Prefer practical, real-world situations over purely theoretical trivia
                - For MCQ: provide 4 options (A-D). Do NOT reveal the correct answer
                - For coding: provide a clear problem statement, I/O format, and 1-2 sample test cases

                When evaluating answers or code:
                - Score 0-10 based on correctness, completeness, and depth
                - Provide professional, constructive feedback
                - Highlight expectations specifically for this role (junior vs senior expectations, backend vs data, etc.)
                - Suggest concrete improvements the candidate should focus on for this role.
                """.formatted(safeRole);
    }

    /**
     * Template for generating interview questions.
     *
     * NOTE: The role and high-level interview type are provided separately via the system prompt.
     */
    public static String generateQuestionPrompt(String questionType,
                                                String interviewType,
                                                String topic,
                                                String difficulty) {
        return String.format("""
                Generate exactly ONE %s question for a %s interview.
                Topic focus: %s
                Difficulty: %s

                Rules:
                - Use the role and conversation context from the system message
                - If MCQ: include 4 options (A-D). Do NOT include the correct answer
                - If SUBJECTIVE: ask one clear question that tests conceptual and practical understanding
                - If BEHAVIORAL: ask about real-world experiences or scenarios relevant to the role
                - If CODING: provide problem statement with input/output format and 1-2 sample test cases

                Output: only the question content to show to the candidate (no JSON, no markdown fences).
                """, questionType, interviewType, topic, difficulty);
    }

    /**
     * Template for evaluating text-based answers (MCQ, Subjective, Behavioral).
     */
    public static String evaluateAnswerPrompt(String questionType,
                                              String topicContext,
                                              String question,
                                              String userAnswer) {
        return String.format("""
                Evaluate this candidate answer for a %s question.

                Topic Context: %s
                Question: %s
                Candidate Answer: %s

                Return STRICT JSON only (no markdown, no code fences):
                {
                  "score": <number 0-10>,
                  "feedback": "<detailed feedback 2-6 sentences>",
                  "explanation": "<what an ideal answer includes>",
                  "suggestions": "<specific improvements for this role>",
                  "is_correct": <true|false>,
                  "correct_answer": "<3-5 lines of a concise, concrete ideal answer tailored to the question>",
                  "reason": "<one sentence explaining why the ideal answer is correct>"
                }
                """, questionType, topicContext, question, userAnswer);
    }

    /**
     * Template for generating coding problems.
     */
    public static String generateCodingProblemPrompt(String topic, String difficulty) {
        return String.format("""
                Create a Java coding interview problem in the Java Full-Stack domain.
                
                Topic Focus: %s
                Difficulty: %s
                
                Include:
                - Problem statement (clear and concise)
                - Input format (stdin format)
                - Output format
                - 1-2 sample inputs/outputs
                - Constraints (if applicable)
                - 2-3 hidden test case descriptions (not full inputs, just descriptions)
                - Recommended approach hints (high level, not full solution)
                
                Output as plain text (not JSON). Format it nicely for display.
                """, topic, difficulty);
    }

    /**
     * Template for evaluating coding solutions.
     */
    public static String evaluateCodingSolutionPrompt(String problem, String code, String output, String hiddenTestsDescription) {
        return String.format("""
                Review this Java code solution.
                
                Problem:
                %s
                
                Hidden test descriptions (may be empty):
                %s
                
                Candidate Code:
                %s
                
                Observed Output/Error from execution:
                %s
                
                Evaluate:
                - Correctness (does it solve the problem?)
                - Efficiency (time/space complexity)
                - Clean code practices (naming, structure, readability)
                - Edge case handling
                
                Return STRICT JSON only (no markdown, no code fences):
                {
                  "score": <number 0-10>,
                  "feedback": "<detailed review covering correctness, complexity, edge cases, Java best practices>",
                  "explanation": "<what an ideal solution/approach looks like>",
                  "suggestions": "<concrete next steps: refactors, tests, improvements>",
                  "is_correct": <true|false>,
                  "correct_answer": "<brief description of the ideal approach or expected output>",
                  "reason": "<short reason why this approach/output is correct>"
                }
                """, problem, 
                hiddenTestsDescription != null ? hiddenTestsDescription : "None provided",
                code, 
                output != null ? output : "No output or error occurred");
    }
}
