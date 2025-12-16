package com.music_app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    // 🔴 PASTE YOUR COPIED API KEY HERE INSIDE THE QUOTES
    private static final String API_KEY = "AIzaSyDGF5pdmm2MsAs9CxcNAB4iz2tkolG3mLs"; 
    
    // We use the "Gemini 1.5 Flash" model which is fast and free
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String getAIResponse(String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. Body: Construct the JSON structure Gemini expects
            // { "contents": [{ "parts": [{ "text": "..." }] }] }
            Map<String, Object> part = new HashMap<>();
            part.put("text", "You are a helpful music assistant. Keep answers brief (under 50 words). User asks: " + userMessage);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. Send Request
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);

            // 4. Parse the Answer
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "The stars are silent today. (No response from AI)";

        } catch (Exception e) {
            e.printStackTrace();
            return "I couldn't reach the AI galaxy. Check your API Key.";
        }
    }
}
