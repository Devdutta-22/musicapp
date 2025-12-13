package com.music_app.controller;

import com.music_app.model.Playlist;
import com.music_app.model.Song;
import com.music_app.repository.PlaylistRepository;
import com.music_app.repository.SongRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    public PlaylistController(PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    // --- REPLACE YOUR EXISTING create METHOD WITH THIS ---
    @PostMapping
    public Playlist create(@RequestBody Playlist p, @RequestHeader("X-User-Id") Long userId) {
        if (p.getName() == null || p.getName().isBlank()) 
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name required");
        
        // Save the User ID so we know who owns this playlist
        p.setUserId(userId);
        
        return playlistRepository.save(p);
    }

    // --- REPLACE YOUR EXISTING list METHOD WITH THIS ---
    @GetMapping
    public List<Playlist> list(@RequestHeader("X-User-Id") Long userId) {
        // Only return playlists for the logged-in user
        return playlistRepository.findByUserId(userId);
    }

    // ... (Keep the rest of your methods: get, addSong, removeSong, delete unchanged) ...
    
    @GetMapping("/{id}")
    public Playlist get(@PathVariable Long id) {
        return playlistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{id}/add/{songId}")
    public Playlist addSong(@PathVariable Long id, @PathVariable Long songId) {
        Playlist pl = playlistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Song s = songRepository.findById(songId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!pl.getSongs().contains(s)) pl.getSongs().add(s);
        return playlistRepository.save(pl);
    }

    @PostMapping("/{id}/remove/{songId}")
    public Playlist removeSong(@PathVariable Long id, @PathVariable Long songId) {
        Playlist pl = playlistRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        pl.getSongs().removeIf(s -> s.getId().equals(songId));
        return playlistRepository.save(pl);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { playlistRepository.deleteById(id); }
}
