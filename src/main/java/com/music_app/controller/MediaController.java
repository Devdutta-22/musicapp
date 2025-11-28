package com.music_app.controller;

import com.music_app.model.Song;
import com.music_app.repository.SongRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final SongRepository songRepository;

    // must be set as environment variable in Render: FILES_BASE_URL
    @Value("${FILES_BASE_URL}")
    private String filesBaseUrl;

    public MediaController(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    /**
     * Redirect /api/audio/{id} -> FILES_BASE_URL/{song.filePath}
     * Example: /api/audio/123 -> https://pub-...r2.dev/songs/xxx.mp3 (302 redirect)
     */
    @GetMapping("/audio/{id}")
    public ResponseEntity<Void> audioRedirect(@PathVariable Long id) {
        Song s = songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String filePath = s.getFilePath(); // e.g. "songs/xxx.mp3"
        if (filePath == null || filePath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String base = normalizeBase(filesBaseUrl);
        String redirectUrl = base + "/" + filePath; // keep slashes intact
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    /**
     * Redirect /api/cover/** to FILES_BASE_URL/{path}
     * Accepts any path after /api/cover/ (covers/..., artists/..., etc.)
     */
    // @GetMapping("/cover/**")
    // public ResponseEntity<Void> coverRedirect(HttpServletRequest request) {
    //     // Get the path within the mapping, e.g. "/cover/covers/xxx.jpg"
    //     String pathWithinHandler = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    //     if (pathWithinHandler == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    //     // Remove the prefix "/cover/" to get "covers/xxx.jpg"
    //     String prefix = "/cover/";
    //     String targetPath;
    //     if (pathWithinHandler.startsWith(prefix)) {
    //         targetPath = pathWithinHandler.substring(prefix.length());
    //     } else {
    //         targetPath = pathWithinHandler; // fallback
    //     }

    //     if (targetPath == null || targetPath.isBlank()) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    //     }

    //     String base = normalizeBase(filesBaseUrl);
    //     String redirectUrl = base + "/" + targetPath;
    //     return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    // }

    // helper to trim trailing slash from base if present
    private String normalizeBase(String base) {
        if (base == null) return "";
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
