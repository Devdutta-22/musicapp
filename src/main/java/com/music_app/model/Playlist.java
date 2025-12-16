package com.music_app.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
public class Playlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long userId; // owner of the playlist

    private Instant createdAt = Instant.now();

    // Changed to EAGER so songs are loaded immediately when you fetch the playlist
    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

    public Playlist() {}
    
    // getters & setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }

    // --- IMPORTANT: This fixes the "0 songs" display ---
    // The frontend looks for a "songCount" property, which this method provides automatically.
    public int getSongCount() {
        return songs.size();
    }
}
