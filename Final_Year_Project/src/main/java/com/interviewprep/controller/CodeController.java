package com.interviewprep.controller;

import com.interviewprep.service.JdoodleService;
import com.interviewprep.service.JdoodleService.CodeExecutionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/code/legacy")
public class CodeController {

    private final JdoodleService jdoodleService;

    public CodeController(JdoodleService jdoodleService) {
        this.jdoodleService = jdoodleService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResult> execute(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String language = body.get("language");
        String stdin = body.get("stdin");
        if (code == null) return ResponseEntity.badRequest().build();
        CodeExecutionResult result = jdoodleService.execute(code, language, stdin);
        return ResponseEntity.ok(result);
    }
}
