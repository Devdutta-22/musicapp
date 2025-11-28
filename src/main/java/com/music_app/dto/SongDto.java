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

        // normalize filesBaseUrl: remove trailing slash if present
        if (filesBaseUrl == null) filesBaseUrl = "";
        if (filesBaseUrl.endsWith("/")) filesBaseUrl = filesBaseUrl.substring(0, filesBaseUrl.length() - 1);

        // cover
        if (s.getCoverPath() != null && !s.getCoverPath().isEmpty()) {
            if (!filesBaseUrl.isEmpty()) {
                d.coverUrl = filesBaseUrl + "/" + s.getCoverPath();
            } else {
                d.coverUrl = "/api/cover/" + s.getCoverPath();
            }
        } else {
            d.coverUrl = null;
        }

        // artist image
        if (s.getArtist() != null &&
            s.getArtist().getImagePath() != null &&
            !s.getArtist().getImagePath().isEmpty()) {

            if (!filesBaseUrl.isEmpty()) {
                d.artistImageUrl = filesBaseUrl + "/" + s.getArtist().getImagePath();
            } else {
                d.artistImageUrl = "/api/cover/" + s.getArtist().getImagePath();
            }
        } else {
            d.artistImageUrl = null;
        }

        // stream url - prefer public files base when present
        if (!filesBaseUrl.isEmpty() && s.getFilePath() != null && !s.getFilePath().isEmpty()) {
            d.streamUrl = filesBaseUrl + "/" + s.getFilePath();
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

        // like data
        d.liked = liked;
        d.likeCount = likeCount;

        return d;
    }
}
