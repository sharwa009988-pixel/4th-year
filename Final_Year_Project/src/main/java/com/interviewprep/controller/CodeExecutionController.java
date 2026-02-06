package com.interviewprep.controller;

import com.interviewprep.service.JdoodleService;
import com.interviewprep.service.JdoodleService.CodeExecutionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/code")
public class CodeExecutionController {

    private final JdoodleService jdoodleService;

    public CodeExecutionController(JdoodleService jdoodleService) {
        this.jdoodleService = jdoodleService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResult> execute(@RequestBody Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        String language = body != null ? body.get("language") : null;
        String stdin = body != null ? body.get("stdin") : null;
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        CodeExecutionResult result = jdoodleService.execute(code, language, stdin);
        return ResponseEntity.ok(result);
    }
}
