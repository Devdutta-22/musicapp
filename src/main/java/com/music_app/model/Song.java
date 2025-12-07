package com.music_app.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "songs")
public class Song {

    @Column(name = "cover_path")
    private String coverPath;

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String album;
    
    // --- NEW FIELD: GENRE ---
    private String genre; 

    private Integer durationSeconds;

    private String filePath;  // Only filename or relative path

    private String mimeType;  // audio/mpeg etc.

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    private Instant createdAt = Instant.now();

    public Song() {}

    // getters & setters
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    
    // --- NEW GETTER & SETTER ---
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
