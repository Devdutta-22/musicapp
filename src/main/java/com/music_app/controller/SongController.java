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

    @Value("${FILES_BASE_URL}")
    private String filesBaseUrl;

    public SongController(SongRepository songRepository, LikeRepository likeRepository, ArtistRepository artistRepository, AmazonS3 s3Client) {
        this.songRepository = songRepository;
        this.likeRepository = likeRepository;
        this.artistRepository = artistRepository;
        this.s3Client = s3Client;
    }

    // --- HELPER: Handle User ID safely ---
    private Long parseUserId(Long headerUserId) {
        // Return 0L for guests so queries don't break
        return headerUserId == null ? 0L : headerUserId;
    }

    // --- HELPER: Map Database Results to DTO ---
    private List<SongDto> mapToDto(List<Object[]> results) {
        return results.stream().map(row -> {
            Song song = (Song) row[0];
            Long likeCount = (Long) row[1];
            Boolean isLiked = (Boolean) row[2];

            // Safe unboxing
            int countVal = likeCount != null ? likeCount.intValue() : 0;
            boolean likedVal = isLiked != null && isLiked;

            return SongDto.fromEntity(song, filesBaseUrl, likedVal, countVal);
        }).collect(Collectors.toList());
    }

    // ==========================================
    //  MOBILE FEEDS & LISTS (NEW)
    // ==========================================

    // 1. HOME FEED: Recently Added (Limit 15)
    @GetMapping("/recent")
    public List<SongDto> getRecent(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return mapToDto(songRepository.findRecentWithLikes(userId));
    }

    // 2. DISCOVERY FEED: Random Songs (Limit 10)
    @GetMapping("/discover")
    public List<SongDto> getDiscovery(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        
        List<Song> randomSongs = songRepository.findRandomSongs();
        
        // Manually check likes since we used a native query for randomness
        return randomSongs.stream().map(s -> {
            int count = likeRepository.countBySongId(s.getId());
            boolean liked = likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();
            return SongDto.fromEntity(s, filesBaseUrl, liked, count);
        }).collect(Collectors.toList());
    }

    // 3. LIBRARY: User's Liked Songs (Personalized Playlist)
    @GetMapping("/liked")
    public List<SongDto> getLikedSongs(@RequestHeader(value = "X-User-Id") Long userIdHeader) {
        if (userIdHeader == null) return Collections.emptyList();
        
        List<Object[]> results = songRepository.findLikedSongs(userIdHeader);
        return mapToDto(results);
    }

    // 4. SEARCH: Optimized Search
    @GetMapping("/search")
    public List<SongDto> search(@RequestParam("q") String q,
                                @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        String ql = q == null ? "" : q.toLowerCase();
        return mapToDto(songRepository.searchWithLikes(ql, userId));
    }

    // 5. ALL SONGS: Main List (Public View)
    @GetMapping
    public List<SongDto> list(@RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = parseUserId(userIdHeader);
        return mapToDto(songRepository.findAllWithLikes(userId));
    }

    // ==========================================
    //  EXISTING UPLOAD LOGIC (UNCHANGED)
    // ==========================================

    private void uploadToR2(MultipartFile file, String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        s3Client.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata));
    }

    private String getExtension(String filename) {
        return (filename != null && filename.contains(".")) ? filename.substring(filename.lastIndexOf('.')) : "";
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SongDto upload(@RequestParam("file") MultipartFile file,
                          @RequestParam("title") String title,
                          @RequestParam(value = "genre", required = false) String genre,
                          @RequestParam(value = "artistId", required = false) Long artistId,
                          @RequestParam(value = "artistName", required = false) String artistName,
                          @RequestParam(value = "artistImage", required = false) MultipartFile artistImage,
                          @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
                          @RequestParam(value = "album", required = false) String album) {

        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");

        try {
            // 1. Upload Song
            String songExt = getExtension(file.getOriginalFilename());
            String songKey = "songs/" + UUID.randomUUID() + songExt;
            uploadToR2(file, songKey);

            // 2. Upload Cover (if present)
            String coverKey = null;
            if (coverImage != null && !coverImage.isEmpty()) {
                String coverExt = getExtension(coverImage.getOriginalFilename());
                coverKey = "covers/" + UUID.randomUUID() + coverExt;
                uploadToR2(coverImage, coverKey);
            }

            // 3. Handle Artist
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

            // 4. Save Song
            Song s = new Song();
            s.setTitle(title != null ? title : file.getOriginalFilename());
            s.setGenre(genre != null ? genre : "Unknown");
            s.setFilePath(songKey);
            s.setMimeType(file.getContentType());
            s.setAlbum(album);
            s.setCoverPath(coverKey);
            s.setArtist(artist);
            songRepository.save(s);

            return SongDto.fromEntity(s, filesBaseUrl, false, 0);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public SongDto getOne(@PathVariable Long id,
                          @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {

        Song s = songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long userId = parseUserId(userIdHeader);
        int count = likeRepository.countBySongId(s.getId());
        boolean liked = (userId != 0) &&
                likeRepository.findByUserIdAndSongId(userId, s.getId()).isPresent();

        return SongDto.fromEntity(s, filesBaseUrl, liked, count);
    }
    
    // Health Check
    @GetMapping("/ping")
    public String ping() {
        return "Pong!";
    }
}
