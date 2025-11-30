package com.music_app.controller;

import com.music_app.model.Lyrics;
import com.music_app.repository.LyricsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * LyricsController — compatible with your Lyrics model (uses getLyrics()/setLyrics()).
 */
@RestController
@RequestMapping("/api/lyrics")
// @CrossOrigin(origins = "*")
@Validated
public class LyricsController {

    private final LyricsRepository lyricsRepository;

    public LyricsController(LyricsRepository lyricsRepository) {
        this.lyricsRepository = lyricsRepository;
    }

    // --- DTO for POST request ---
    public static class LyricsRequest {
        public Long songId;
        public String lyrics;      // text content
        public String source;      // optional metadata

        public LyricsRequest() {}
    }

    // --- Helper to build stable response ---
    private Map<String, Object> buildResponse(Long songId, String lyricsText, String source) {
        Map<String, Object> res = new HashMap<>();
        res.put("songId", songId);
        res.put("lyrics", lyricsText == null ? "" : lyricsText);
        res.put("source", source);
        return res;
    }

    /**
     * GET /api/lyrics?songId=...
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getByQuery(@RequestParam Long songId) {
        try {
            Optional<Lyrics> opt = lyricsRepository.findById(songId);
            if (opt.isPresent()) {
                Lyrics l = opt.get();
                String text = l.getLyrics() != null ? l.getLyrics() : "";
                return ResponseEntity.ok(buildResponse(songId, text, l.getSource()));
            } else {
                return ResponseEntity.ok(buildResponse(songId, "", null));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }

    /**
     * GET /api/lyrics/{songId}
     */
    @GetMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> getByPath(@PathVariable Long songId) {
        return getByQuery(songId);
    }

    /**
     * POST /api/lyrics
     * Body: { songId, lyrics, source? }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrUpdate(@RequestBody LyricsRequest req) {
        if (req == null || req.songId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "songId required"));
        }

        try {
            String incomingText = req.lyrics == null ? "" : req.lyrics;
            String source = req.source == null ? "manual" : req.source;

            Optional<Lyrics> existing = lyricsRepository.findById(req.songId);

            Lyrics saved;
            if (existing.isPresent()) {
                Lyrics e = existing.get();
                e.setLyrics(incomingText);
                e.setSource(source);
                saved = lyricsRepository.save(e);
            } else {
                Lyrics l = new Lyrics(req.songId, incomingText, source);
                saved = lyricsRepository.save(l);
            }

            String text = saved.getLyrics() != null ? saved.getLyrics() : "";
            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("entry", buildResponse(saved.getSongId(), text, saved.getSource()));
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }

    /**
     * DELETE /api/lyrics/{songId}
     */
    @DeleteMapping("/{songId}")
    public ResponseEntity<Map<String, Object>> deleteByPath(@PathVariable Long songId) {
        try {
            if (lyricsRepository.existsById(songId)) {
                lyricsRepository.deleteById(songId);
            }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "internal", "message", ex.getMessage()));
        }
    }
}
