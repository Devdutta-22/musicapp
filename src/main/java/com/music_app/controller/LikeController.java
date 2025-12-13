package com.music_app.controller;

import com.music_app.model.LikeEntity;
import com.music_app.repository.LikeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeRepository likeRepo;

    public LikeController(LikeRepository likeRepo) {
        this.likeRepo = likeRepo;
    }

    // --- 1. LIKE A SONG (Personalized) ---
    // Saves the like with the specific userId from the header
    @PostMapping("/{songId}")
    public ResponseEntity<?> likeSong(@PathVariable Long songId, @RequestHeader("X-User-Id") Long userId) {
        // Check if this specific user already liked the song
        if (likeRepo.findByUserIdAndSongId(userId, songId).isPresent()) {
            return ResponseEntity.ok("Already liked");
        }

        LikeEntity like = new LikeEntity();
        like.setUserId(userId); // <--- This is the key to personalization
        like.setSongId(songId);
        like.setCreatedAt(Instant.now());

        likeRepo.save(like);
        return ResponseEntity.ok("Liked");
    }

    // --- 2. UNLIKE A SONG (Personalized) ---
    // Only deletes the like if it belongs to this user
    @DeleteMapping("/{songId}")
    public ResponseEntity<?> unlikeSong(@PathVariable Long songId, @RequestHeader("X-User-Id") Long userId) {
        Optional<LikeEntity> like = likeRepo.findByUserIdAndSongId(userId, songId);
        
        // Only delete if found for THIS user
        like.ifPresent(likeRepo::delete);
        
        return ResponseEntity.ok("Unliked");
    }
    
    // --- 3. GET LIKE COUNT (Public) ---
    // Anyone can see how many times a song was liked total
    @GetMapping("/{songId}/count")
    public int getLikeCount(@PathVariable Long songId) {
        return likeRepo.countBySongId(songId);
    }
}
