package com.interviewprep.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OfflineQuestionBank {

    private final Map<String, List<String>> subjectiveQuestions = new HashMap<>();
    private final Map<String, List<String>> mcqQuestions = new HashMap<>();
    private final Map<String, List<String>> codingQuestions = new HashMap<>();

    public OfflineQuestionBank() {
        initializeSubjective();
        initializeMcq();
        initializeCoding();
    }

    public String getRandomQuestion(String topic, String type, String difficulty) {
        if ("MCQ".equalsIgnoreCase(type)) {
            return getRandom(mcqQuestions, topic);
        } else if ("CODING".equalsIgnoreCase(type)) {
            return getRandom(codingQuestions, topic);
        } else {
            return getRandom(subjectiveQuestions, topic);
        }
    }

    public String getMockEvaluation(String questionType) {
        if ("MCQ".equalsIgnoreCase(questionType)) {
             return "{\"feedback\": \"Correct answer! (Offline Mode)\", \"score\": 10.0, \"is_correct\": true}";
        }
        return "{\"feedback\": \"This is a simulated evaluation because the AI service is currently offline (No Credits). Your answer seems relevant and covers key points. To get real AI feedback, please add credits to your xAI account.\", \"score\": 8.5, \"is_correct\": true}";
    }
    
    public String getMockCodingEvaluation() {
        return "{\"feedback\": \"Code compilation successful. (Offline Mode - AI evaluation unavailable). Your logic appears structured.\", \"score\": 8.0}";
    }

    private String getRandom(Map<String, List<String>> map, String topic) {
        List<String> questions = new ArrayList<>();
        
        // Try to find specific topic match
        if (topic != null) {
            for (String key : map.keySet()) {
                if (key.toLowerCase().contains(topic.toLowerCase()) || topic.toLowerCase().contains(key.toLowerCase())) {
                    questions.addAll(map.get(key));
                }
            }
        }
        
        // If no specific topic found or list is empty, add 'General' and others to ensure variety
        if (questions.isEmpty()) {
            map.values().forEach(questions::addAll);
        }

        if (questions.isEmpty()) return "Describe a challenging technical problem you solved recently.";

        return questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
    }

    private void initializeSubjective() {
        List<String> java = List.of(
            "Explain the difference between checked and unchecked exceptions in Java. When would you use each?",
            "How does the Java Garbage Collector work? Describe the generational heap structure.",
            "What is the contract between equals() and hashCode()? What happens if you break it?",
            "Explain the difference between an Interface and an Abstract Class in Java 8+.",
            "What are Java Streams? Explain intermediate vs terminal operations with examples.",
            "Describe the Singleton pattern. How can you make it thread-safe in Java?",
            "What is the volatile keyword in Java and how does it relate to the Java Memory Model?",
            "Explain the difference between fail-fast and fail-safe iterators.",
            "How does HashMap work internally? What happens during a collision?",
            "What is Dependency Injection? How does Spring Framework implement it?"
        );
        subjectiveQuestions.put("Java", java);

        List<String> react = List.of(
            "What is the Virtual DOM in React and how does it improve performance?",
            "Explain the React Component Lifecycle (mounting, updating, unmounting).",
            "What are React Hooks? Compare useEffect with componentDidMount.",
            "What is the difference between State and Props in React?",
            "How do you handle forms in React? Controlled vs Uncontrolled components.",
            "What is the Context API? When should you use it over Redux?",
            "Explain Higher-Order Components (HOC) with an example.",
            "What is Prop Drilling and how can you avoid it?",
            "How does React Router work? Explain the difference between BrowserRouter and HashRouter.",
            "What is the significance of keys in React lists?"
        );
        subjectiveQuestions.put("React", react);

        List<String> general = List.of(
            "Explain the CAP theorem in the context of distributed systems.",
            "What is a REST API? Describe its key constraints.",
            "Difference between SQL and NoSQL databases. When to choose which?",
            "What is CI/CD? Describe a typical pipeline.",
            "Explain the concept of microservices architecture vs monolithic.",
            "What is OAuth 2.0? Describe the authorization code flow.",
            "How does HTTPS work? Explain the SSL/TLS handshake.",
            "What is Docker? How is it different from a Virtual Machine?",
            "Explain the SOLID principles of object-oriented design.",
            "What is an Index in a database? How does it improve query performance?"
        );
        subjectiveQuestions.put("General", general);
    }

    private void initializeMcq() {
        List<String> java = List.of(
            "Which of these is NOT a primitive type in Java?\n\nA) int\nB) float\nC) String\nD) boolean",
            "What is the default value of a local variable in Java?\n\nA) null\nB) 0\nC) false\nD) No default value (compilation error)",
            "Which collection does not allow duplicate elements?\n\nA) List\nB) Set\nC) Queue\nD) Map",
            "What is the size of an int in Java?\n\nA) 16 bit\nB) 32 bit\nC) 64 bit\nD) 8 bit",
            "Which keyword is used to prevent method overriding?\n\nA) static\nB) final\nC) abstract\nD) const"
        );
        mcqQuestions.put("Java", java);

        List<String> web = List.of(
            "Which HTTP status code indicates 'Not Found'?\n\nA) 200\nB) 301\nC) 404\nD) 500",
            "What does CSS stand for?\n\nA) Creative Style Sheets\nB) Cascading Style Sheets\nC) Computer Style Sheets\nD) Colorful Style Sheets",
            "Which HTML tag is used for the largest heading?\n\nA) <heading>\nB) <h6>\nC) <h1>\nD) <head>",
            "Which JavaScript method removes the last element from an array?\n\nA) shift()\nB) pop()\nC) push()\nD) unshift()",
            "What is the default method of a form submission?\n\nA) GET\nB) POST\nC) PUT\nD) DELETE"
        );
        mcqQuestions.put("Web", web);
    }

    private void initializeCoding() {
        List<String> algo = List.of(
            "Write a function to check if a string is a palindrome. (Ignore case and non-alphanumeric characters)",
            "Implement a function to find the nth Fibonacci number using dynamic programming.",
            "Given an array of integers, find two numbers such that they add up to a specific target.",
            "Write a program to reverse a linked list.",
            "Implement a function to validate if a given string has balanced parentheses: '()', '{}', '[]'.",
            "Write a function to find the longest substring without repeating characters.",
            "Merge two sorted arrays into a single sorted array.",
            "Find the maximum subarray sum (Kadane's Algorithm).",
            "Implement Binary Search on a sorted array.",
            "Write a function to detect a cycle in a linked list."
        );
        codingQuestions.put("Algorithms", algo);
    }
}
