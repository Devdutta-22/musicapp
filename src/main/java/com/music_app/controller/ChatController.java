package com.music_app.controller;

import com.music_app.service.AIService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
// 🔴 REMOVED THE CONFLICTING ANNOTATION: @CrossOrigin(origins = "*")
public class ChatController {

    private final AIService aiService;

    public ChatController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String aiReply = aiService.getAIResponse(userMessage);
        return ResponseEntity.ok(Map.of("reply", aiReply));
    }
}
