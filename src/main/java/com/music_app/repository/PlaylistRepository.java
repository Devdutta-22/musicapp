package com.music_app.repository;

import com.music_app.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; 

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // Finds playlists created by a specific user
    List<Playlist> findByUserId(Long userId);
}
