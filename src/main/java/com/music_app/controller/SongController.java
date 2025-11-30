package com.music_app.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.music_app.dto.SongDto;
import com.music_app.model.Artist;
import com.music_app.model.LikeEntity;
import com.music_app.model.Song;
import com.music_app.repository.ArtistRepository;
import com.music_app.repository.LikeRepository;
import com.music_app.repository.SongRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository;
    private final LikeRepository likeRepository;
    private final ArtistRepository artistRepository;
    private final AmazonS3 s3Client; 

    @Value("${r2.bucket-name}")
    private String bucketName;

    // CHANGED: Matches the variable in your screenshot
    @Value("${FILES_BASE_URL}") 
    private String filesBaseUrl;

    public SongController(SongRepository songRepository, LikeRepository likeRepository, ArtistRepository artistRepository, AmazonS3 s3Client) {
        this.songRepository = songRepository;
        this.likeRepository = likeRepository;
        this.artistRepository = artistRepository;
        this.s3Client = s3Client;
    }

    // --- Helper to Upload to Cloudflare ---
    private void uploadToR2(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        s3Client.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata));
    }

    // --- UPLOAD ENDPOINT ---
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
            // 1. Upload Song to Cloudflare
            String songExt = getExtension(file.getOriginalFilename());
            String songKey = "songs/" + UUID.randomUUID() + songExt;
            uploadToR2(file, songKey);

            // 2. Upload Cover to Cloudflare (if present)
            String coverKey = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                String coverExt = getExtension(coverImage.getOriginalFilename());
                coverKey = "covers/" + UUID.randomUUID() + coverExt;
                uploadToR2(coverImage, coverKey);
            }

            // 3. Handle Artist Logic
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

            // 4. Save to Database
            Song s = new Song();
            s.setTitle(title != null ? title : file.getOriginalFilename());
            s.setFilePath(songKey); 
            s.setMimeType(file.getContentType());
            s.setAlbum(album);
            s.setCoverPath(coverKey);
            s.setArtist(artist);
            songRepository.save(s);

            // Return DTO
            return SongDto.fromEntity(s, filesBaseUrl, false, 0);

        } catch (IOException e) {
            e.printStackTrace(); 
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        return (filename != null && filename.contains(".")) ? filename.substring(filename.lastIndexOf('.')) : "";
    }
    
    // ... (Keep the rest of your Get/List/Search methods here) ...
    // Make sure you include the search, list, getOne, and like/unlike methods!
    
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
    @GetMapping("/ping")
    public String ping() {
        return "Pong!";
    }
}
