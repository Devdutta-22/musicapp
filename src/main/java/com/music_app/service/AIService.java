package com.music_app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    // ✅ Your API Key
    private static final String API_KEY = "AIzaSyDLHjXqlOg0Oizt0VX3dWgWeSRu-syMf64"; 
    
    // ✅ Using the latest Free Tier friendly model
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public String getAIResponse(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 1. Construct the Prompt
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
            return "Error: " + e.getMessage(); 
        }
    }
}
