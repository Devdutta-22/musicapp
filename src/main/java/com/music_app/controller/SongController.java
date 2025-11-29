package com.music_app.controller;

import com.music_app.dto.SongDto;
import com.music_app.model.Artist;
import com.music_app.model.LikeEntity;
import com.music_app.model.Song;
import com.music_app.repository.ArtistRepository;
import com.music_app.repository.LikeRepository;
import com.music_app.repository.SongRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;
    private final LikeRepository likeRepository;
    private final ArtistRepository artistRepository;

    @Value("${app.media.base-path}")
    private String mediaBasePath;

    // 🔥 Use storage.base-url from application.properties (R2 public base)
    @Value("${storage.base-url:}")
    private String filesBaseUrl;

    public SongController(SongRepository songRepository, LikeRepository likeRepository, ArtistRepository artistRepository) {
        this.songRepository = songRepository;
        this.likeRepository = likeRepository;
        this.artistRepository = artistRepository;
    }

    private Long parseUserId(Long headerUserId) {
        return headerUserId == null ? null : headerUserId;
    }

    @GetMapping("/search")
    public List<SongDto> search(@RequestParam("q") String q,
                                @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {

        String ql = q == null ? "" : q.toLowerCase();
        List<Song> matched = songRepository.findAll().stream()
                .filter(s -> (s.getTitle() != null && s.getTitle().toLowerCase().contains(ql)) ||
                        (s.getArtist() != null && s.getArtist().getName() != null && s.getArtist().getName().toLowerCase().contains(ql)))
                .collect(Collectors.toList());

        Long userId = parseUserId(userIdHeader);

        return matched.stream()
                .map(s -> {
                    int count = likeRepository.countBySongId(s.getId());
                    boolean liked = (userId != null) &&
                            likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
                    return SongDto.fromEntity(s, filesBaseUrl, liked, count);
                })
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<SongDto> list(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);

        return songRepository.findAll().stream()
                .map(s -> {
                    int count = likeRepository.countBySongId(s.getId());
                    boolean liked = (userId != null) &&
                            likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
                    return SongDto.fromEntity(s, filesBaseUrl, liked, count);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SongDto getOne(@PathVariable Long id,
                          @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {

        Song s = songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long userId = parseUserId(userIdHeader);
        int count = likeRepository.countBySongId(s.getId());
        boolean liked = (userId != null) &&
                likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();

        return SongDto.fromEntity(s, filesBaseUrl, liked, count);
    }

    @PostMapping("/{id}/like")
    public Map<String, Object> like(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var existing = likeRepository.findByUserIdAndSongId(userId, id);
        if (existing.isPresent()) {
            int count = likeRepository.countBySongId(id);
            return Map.of("liked", true, "likeCount", count);
        }

        LikeEntity l = new LikeEntity();
        l.setUserId(userId);
        l.setSongId(id);
        likeRepository.save(l);

        int count = likeRepository.countBySongId(id);
        return Map.of("liked", true, "likeCount", count);
    }

    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlike(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        if (!songRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        var existing = likeRepository.findByUserIdAndSongId(userId, id);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
        }

        int count = likeRepository.countBySongId(id);
        return Map.of("liked", false, "likeCount", count);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SongDto upload(@RequestParam("file") MultipartFile file,
                          @RequestParam("title") String title,
                          @RequestParam(value = "artistId", required = false) Long artistId,
                          @RequestParam(value = "artistName", required = false) String artistName,
                          @RequestParam(value = "artistImage", required = false) MultipartFile artistImage,
                          @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                          @RequestParam(value = "album", required = false) String album) {

        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");

        try {
            Path base = Paths.get(mediaBasePath);
            if (!Files.exists(base)) Files.createDirectories(base);

            // --- Save file locally (only for fallback mode) ---
            String original = file.getOriginalFilename();
            String ext = original != null && original.contains(".")
                    ? original.substring(original.lastIndexOf('.'))
                    : ".mp3";

            String savedName = UUID.randomUUID().toString() + ext;

            Path songsFolder = base.resolve("songs");
            Files.createDirectories(songsFolder);
            Files.copy(file.getInputStream(), songsFolder.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);

            // --- Save cover ---
            String coverSavedName = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                String extCover = coverImage.getOriginalFilename().contains(".")
                        ? coverImage.getOriginalFilename().substring(coverImage.getOriginalFilename().lastIndexOf('.'))
                        : ".png";

                coverSavedName = UUID.randomUUID().toString() + extCover;

                Path coverFolder = base.resolve("covers");
                Files.createDirectories(coverFolder);
                Files.copy(coverImage.getInputStream(), coverFolder.resolve(coverSavedName), StandardCopyOption.REPLACE_EXISTING);
            }

            // --- Artist handling ---
            Artist artist = null;

            if (artistId != null && artistRepository.existsById(artistId)) {
                artist = artistRepository.findById(artistId).get();
            } else if (artistName != null && !artistName.isBlank()) {
                artist = artistRepository.findByName(artistName.trim())
                        .orElseGet(() -> {
                            Artist a = new Artist();
                            a.setName(artistName.trim());
                            return artistRepository.save(a);
                        });
            }

            // --- Create song entity ---
            Song s = new Song();
            s.setTitle(title != null ? title : original);
            s.setFilePath("songs/" + savedName);
            s.setMimeType(file.getContentType());

            if (album != null) s.setAlbum(album);
            if (coverSavedName != null) s.setCoverPath("covers/" + coverSavedName);
            if (artist != null) s.setArtist(artist);

            songRepository.save(s);

            return SongDto.fromEntity(s, filesBaseUrl, false, 0);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", e);
        }
    }

}
