package com.music_app.controller;

import com.music_app.model.User;
import com.music_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // THIS is the specific endpoint your frontend is looking for!
    @PostMapping("/{id}/add-minutes")
    public ResponseEntity<?> addMinutes(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        return userRepository.findById(id).map(user -> {
            int newMinutes = payload.get("minutes");
            // Add new minutes to the existing total
            user.setTotalMinutesListened(user.getTotalMinutesListened() + newMinutes);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "totalMinutes", user.getTotalMinutesListened(),
                "message", "Time updated!"
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}
