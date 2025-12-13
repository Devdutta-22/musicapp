package com.music_app.controller;

import com.music_app.model.Playlist;
import com.music_app.model.Song;
import com.music_app.repository.PlaylistRepository;
import com.music_app.repository.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public PlaylistController(PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    // --- 1. CREATE (Fixed to save User ID) ---
    @PostMapping
    public Playlist create(@RequestBody Playlist p, @RequestHeader("X-User-Id") Long userId) {
        if (p.getName() == null || p.getName().isBlank()) 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        
        p.setUserId(userId); // Save the owner
        return playlistRepository.save(p);
    }

    // --- 2. LIST (Fixed to show ONLY your playlists) ---
    @GetMapping
    public List<Playlist> list(@RequestHeader("X-User-Id") Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    // --- 3. ADD SONG (This fixes the "Failed to Add" error) ---
    // Matches the Frontend call to /api/playlists/{id}/songs
    @PostMapping("/{id}/songs")
    public Playlist addSongFromBody(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        Long songId = payload.get("songId");
        if (songId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Song ID required");

        Playlist pl = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
        
        Song s = songRepository.findById(songId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));
        
        // Add only if not already present to prevent duplicates
        if (!pl.getSongs().contains(s)) {
            pl.getSongs().add(s);
        }
        
        return playlistRepository.save(pl);
    }

    // --- Get Single Playlist ---
    @GetMapping("/{id}")
    public Playlist get(@PathVariable Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // --- Remove Song ---
    @DeleteMapping("/{id}/songs/{songId}")
    public Playlist removeSong(@PathVariable Long id, @PathVariable Long songId) {
        Playlist pl = playlistRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        pl.getSongs().removeIf(s -> s.getId().equals(songId));
        return playlistRepository.save(pl);
    }

    // --- Delete Playlist ---
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { 
        playlistRepository.deleteById(id); 
    }
}
