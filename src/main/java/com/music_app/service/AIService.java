package com.music_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AIService {

    // 🟢 SECURE: Read key from Environment Variables
    @Value("${gemini.api.key}")
    private String apiKey;

    // 🟢 UPDATED: Use the latest Gemini 2.5 Flash model
    private static final String MODEL_ID = "gemini-2.5-flash";

    public String getAIResponse(String userMessage) {
        try {
            // Check if key is loaded
            if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("INSERT")) {
                return "Error: Server API Key is missing. Please check Render Environment Variables.";
            }

            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_ID + ":generateContent?key=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Construct the Prompt
            Map<String, Object> part = new HashMap<>();
            part.put("text", "You are a helpful AI assistant. Answer the user's question concisely. User asks: " + userMessage);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Send Request
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // Parse Response
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
