package com.music_app.dto;

import com.music_app.model.Song;

public class SongDto {

    public Long id;
    public String title;
    public String album;
    public Integer durationSeconds;
    public String artistName;
    public String streamUrl;
    public String mimeType;

    public String coverUrl;
    public String artistImageUrl;
    
    // --- NEW FIELD: GENRE ---
    public String genre;

    public boolean liked;
    public int likeCount;

    // Backward-compatible simple method (keeps old signature)
    public static SongDto fromEntity(Song s) {
        return fromEntity(s, "", false, 0);
    }

    // Keep existing two-arg method for compatibility (if any)
    public static SongDto fromEntity(Song s, boolean liked, int likeCount) {
        return fromEntity(s, "", liked, likeCount);
    }

    // NEW: full method with filesBaseUrl
    public static SongDto fromEntity(Song s, String filesBaseUrl, boolean liked, int likeCount) {
        SongDto d = new SongDto();

        // normalize filesBaseUrl: null-safe, remove newlines, trim and remove trailing slash
        if (filesBaseUrl == null) filesBaseUrl = "";
        // remove all CR/LF and trim outer whitespace
        filesBaseUrl = filesBaseUrl.replace("\r", "").replace("\n", "").trim();
        // remove trailing slash if present
        while (filesBaseUrl.endsWith("/")) {
            filesBaseUrl = filesBaseUrl.substring(0, filesBaseUrl.length() - 1);
        }

        // helper to join base + relative path safely
        java.util.function.BiFunction<String, String, String> join = (base, rel) -> {
            if (base == null || base.isEmpty()) return rel;
            if (rel == null || rel.isEmpty()) return base;
            rel = rel.replaceAll("^/+", ""); // remove leading slashes
            return base + "/" + rel;
        };

        // cover
        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            if (!filesBaseUrl.isEmpty()) {
                d.coverUrl = join.apply(filesBaseUrl, s.getCoverPath()).replace("\r", "").replace("\n", "").trim();
            } else {
                d.coverUrl = ("/api/cover/" + s.getCoverPath()).replace("\r", "").replace("\n", "").trim();
            }
        } else {
            d.coverUrl = null;
        }

        // artist image
        if (s.getArtist() != null &&
            s.getArtist().getImagePath() != null &&
            !s.getArtist().getImagePath().isEmpty()) {

            if (!filesBaseUrl.isEmpty()) {
                d.artistImageUrl = join.apply(filesBaseUrl, s.getArtist().getImagePath()).replace("\r", "").replace("\n", "").trim();
            } else {
                d.artistImageUrl = ("/api/cover/" + s.getArtist().getImagePath()).replace("\r", "").replace("\n", "").trim();
            }
        } else {
            d.artistImageUrl = null;
        }

        // stream url - prefer public files base when present
        if (!filesBaseUrl.isEmpty() && s.getFilePath() != null && !s.getFilePath().isEmpty()) {
            d.streamUrl = join.apply(filesBaseUrl, s.getFilePath()).replace("\r", "").replace("\n", "").trim();
        } else {
            // fallback to server-stream endpoint by id (existing behavior)
            d.streamUrl = "/api/audio/" + s.getId();
        }

        d.id = s.getId();
        d.title = s.getTitle();
        d.album = s.getAlbum();
        d.durationSeconds = s.getDurationSeconds();
        d.mimeType = s.getMimeType();
        d.artistName = s.getArtist() != null ? s.getArtist().getName() : null;
        
        // --- POPULATE GENRE ---
        d.genre = s.getGenre();

        // like data
        d.liked = liked;
        d.likeCount = likeCount;

        return d;
    }
}
