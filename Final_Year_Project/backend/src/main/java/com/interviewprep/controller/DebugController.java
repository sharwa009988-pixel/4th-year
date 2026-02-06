package com.interviewprep.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Value("${jdoodle.client-id:}")
    private String jdClientId;

    @Value("${jdoodle.client-secret:}")
    private String jdClientSecret;

    @GetMapping("/jdoodle")
    public ResponseEntity<Map<String, String>> jdoodle() {
        String idMasked = mask(jdClientId);
        String secretMasked = mask(jdClientSecret);
        return ResponseEntity.ok(Map.of("clientId", idMasked, "clientSecret", secretMasked));
    }

    private String mask(String s) {
        if (s == null) return "";
        if (s.length() <= 4) return "****";
        return "****" + s.substring(s.length() - 4);
    }
}
