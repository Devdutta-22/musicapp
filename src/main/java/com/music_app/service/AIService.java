package com.music_app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    // ✅ Keep your working API Key
    private static final String API_KEY = "AIzaSyDANbOjG2nlDGt9dqRi9Q2iBPBTdywUXGI"; 
    
    // 🔴 CHANGE: Use 'gemini-1.5-flash-001' (Exact Version) instead of generic name
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-001:generateContent?key=" + API_KEY;

    public String getAIResponse(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. Tell AI to be a "Helpful Assistant" (General Purpose)
            Map<String, Object> part = new HashMap<>();
            part.put("text", "You are a helpful AI assistant. Answer the user's question concisely. User asks: " + userMessage);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 2. Send Request
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // 3. Parse Response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "I couldn't think of an answer. (Empty response from AI)";

        } catch (Exception e) {
            e.printStackTrace(); 
            // This will show the error in the chat for easier debugging
            return "Error: " + e.getMessage(); 
        }
    }
}
