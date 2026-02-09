package ru.healthfamily.biobank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/test-endpoint")
public class TestController {
    
    @GetMapping
    public Map<String, String> getTest() {
        return Map.of(
            "message", "Test endpoint работает!",
            "status", "OK",
            "timestamp", LocalDateTime.now().toString()
        );
    }
}