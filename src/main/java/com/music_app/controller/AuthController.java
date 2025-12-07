package com.music_app.controller;

import com.music_app.model.User;
import com.music_app.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTER (Join the Galaxy)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 1. Check if username exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken!");
        }
        
        // 2. Set default planet stats for new user
        user.setPlanetName(user.getUsername() + "'s World");
        user.setPlanetType("Nebula Cloud"); // Everyone starts as a Nebula
        user.setTotalMinutesListened(0);
        
        // 3. Save to DB
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // LOGIN (Enter the Galaxy)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        // Simple password check
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            // Return the User ID and Planet Info so frontend can use it
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "planetType", user.getPlanetType() != null ? user.getPlanetType() : "Unknown",
                "planetName", user.getPlanetName() != null ? user.getPlanetName() : "Unknown"
            ));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
