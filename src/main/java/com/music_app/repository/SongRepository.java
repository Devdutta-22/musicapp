package com.music_app.repository;

import com.music_app.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // 1. Existing Search
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "(SELECT COUNT(l) > 0 FROM LikeEntity l WHERE l.songId = s.id AND l.userId = :userId) AS isLiked " +
           "FROM Song s " +
           "WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Object[]> searchWithLikes(@Param("q") String q, @Param("userId") Long userId);

    // 2. Existing List All
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "(SELECT COUNT(l) > 0 FROM LikeEntity l WHERE l.songId = s.id AND l.userId = :userId) AS isLiked " +
           "FROM Song s")
    List<Object[]> findAllWithLikes(@Param("userId") Long userId);

    // --- MISSING METHODS ADDED BELOW ---

    // 3. NEW: Recently Added (Limit 15)
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "(SELECT COUNT(l) > 0 FROM LikeEntity l WHERE l.songId = s.id AND l.userId = :userId) AS isLiked " +
           "FROM Song s " +
           "ORDER BY s.createdAt DESC LIMIT 15")
    List<Object[]> findRecentWithLikes(@Param("userId") Long userId);

    // 4. NEW: Discovery / Random (Limit 10)
    @Query(value = "SELECT * FROM songs ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Song> findRandomSongs(); 

    // 5. NEW: Fetch ONLY Liked Songs for a specific user
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "true AS isLiked " + 
           "FROM Song s " +
           "JOIN LikeEntity userLike ON s.id = userLike.songId " +
           "WHERE userLike.userId = :userId " +
           "ORDER BY userLike.createdAt DESC") 
    List<Object[]> findLikedSongs(@Param("userId") Long userId);
}
