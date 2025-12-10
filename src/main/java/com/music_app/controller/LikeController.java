package com.music_app.controller;

import com.music_app.model.LikeEntity;
import com.music_app.repository.LikeRepository;
import com.music_app.repository.SongRepository;
import com.music_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeRepository likeRepo;
    // Removed unused repos for cleaner code

    public LikeController(LikeRepository likeRepo) {
        this.likeRepo = likeRepo;
    }

    // --- 1. LIKE A SONG (User Specific) ---
    @PostMapping("/{songId}")
    public ResponseEntity<?> likeSong(@PathVariable Long songId, @RequestHeader("X-User-Id") Long userId) {
        // Check if already liked by THIS specific user
        if (likeRepo.findByUserIdAndSongId(userId, songId).isPresent()) {
            return ResponseEntity.ok("Already liked");
        }

        LikeEntity like = new LikeEntity();
        like.setUserId(userId); // <--- Uses the actual logged-in ID
        like.setSongId(songId);
        like.setCreatedAt(Instant.now());

        likeRepo.save(like);
        return ResponseEntity.ok("Liked");
    }

    // --- 2. UNLIKE A SONG (User Specific) ---
    @DeleteMapping("/{songId}")
    public ResponseEntity<?> unlikeSong(@PathVariable Long songId, @RequestHeader("X-User-Id") Long userId) {
        Optional<LikeEntity> like = likeRepo.findByUserIdAndSongId(userId, songId);
        like.ifPresent(likeRepo::delete);
        return ResponseEntity.ok("Unliked");
    }

    // --- 3. GET LIKE COUNT (Public info) ---
    @GetMapping("/{songId}")
    public int getLikeCount(@PathVariable Long songId) {
        return likeRepo.countBySongId(songId);
    }
}
