package com.music_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // ✅ Using the High-Limit Stable Model
   private static final String MODEL_ID = "gemini-1.5-flash-002";
    public String getAIResponse(String userMessage) {
        try {
            if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("INSERT")) {
                return "Error: Server API Key is missing. Please check Render Environment Variables.";
            }

            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_ID + ":generateContent?key=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 🎵 CUSTOM PERSONA PROMPT
            String personaInstruction = 
                "You are the AI Guide for 'Astronote', a space-themed music application. " +
                "Your persona is a 'Cosmic DJ'. " +
                "Guidelines: " +
                "1. Answer the user's question clearly and accurately. " +
                "2. If the topic is music, show deep knowledge. " +
                "3. If the topic is NOT music, still answer it, but try to add a subtle musical or cosmic flair to your tone (e.g., mention frequencies, vibes, orbits, or harmony). " +
                "4. Keep your answer balanced: not too short (one word), but not an essay. Aim for 2-4 sentences unless a list is requested. " +
                "User asks: " + userMessage;

            Map<String, Object> part = new HashMap<>();
            part.put("text", personaInstruction);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResp.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
            return "The signal is lost in the void. (Empty response)";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error contacting the mothership: " + e.getMessage();
        }
    }
}
