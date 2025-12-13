package com.music_app.repository;

import com.music_app.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <--- Import this

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // --- ADD THIS LINE ---
    List<Playlist> findByUserId(Long userId);
}
