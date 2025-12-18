package com.music_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    // 🟢 INJECT MULTIPLE KEYS (Comma Separated)
    @Value("${gemini.api.keys:}") 
    private String apiKeysString;

    // Use the stable model
    private static final String MODEL_ID = "gemini-2.5-flash";

    public String getAIResponse(String userMessage) {
        // 1. Parse Keys
        if (apiKeysString == null || apiKeysString.isEmpty()) {
            return "Error: No API Keys found. Check Render Environment Variables.";
        }
        
        String[] keys = apiKeysString.split(",");
        List<String> validKeys = new ArrayList<>();
        for (String k : keys) {
            if (!k.trim().isEmpty()) validKeys.add(k.trim());
        }

        if (validKeys.isEmpty()) return "Error: API Key list is empty.";

        // 2. Loop through Keys (Fallback Logic)
        for (int i = 0; i < validKeys.size(); i++) {
            String currentKey = validKeys.get(i);
            try {
                return callGemini(currentKey, userMessage);
            } catch (HttpClientErrorException e) {
                // If it's a 429 (Too Many Requests) or 403 (Quota/Ban), try next key
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    System.out.println("⚠️ Key #" + (i+1) + " failed (" + e.getStatusCode() + "). Switching to next key...");
                    continue; // Try next key
                }
                // If it's another error (like 400 Bad Request), don't retry, just fail
                return "Error from AI: " + e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return "System Error: " + e.getMessage();
            }
        }

        return "All API keys are currently exhausted or busy. Please try again later.";
    }

    // Helper method to make the actual call
    private String callGemini(String apiKey, String userMessage) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_ID + ":generateContent?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String personaInstruction = 
            "You are the AI Guide for 'Astronote', a space-themed music application. " +
            "Your persona is a 'Cosmic DJ'. " +
            "Guidelines: " +
            "1. Answer the user's question clearly and accurately. " +
            "2. If the topic is music, show deep knowledge. " +
            "3. If the topic is NOT music, still answer it, but try to add a subtle musical or cosmic flair to your tone. " +
            "4. Keep your answer balanced: not too short, but not an essay. " +
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
        throw new RuntimeException("Empty response from Gemini");
    }
}
