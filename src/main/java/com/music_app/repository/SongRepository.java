package com.music_app.repository;

import com.music_app.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    // This SQL query runs inside the database (Super Fast)
    List<Song> findByTitleContainingIgnoreCaseOrArtistNameContainingIgnoreCase(String title, String artistName);
}
