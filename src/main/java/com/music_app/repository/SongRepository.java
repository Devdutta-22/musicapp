package com.music_app.repository;

import com.music_app.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    // 1. Optimized Search: Fetches Song + Count + Liked Status in ONE query
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "(SELECT COUNT(l) > 0 FROM LikeEntity l WHERE l.songId = s.id AND l.userId = :userId) AS isLiked " +
           "FROM Song s " +
           "WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(s.artist.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Object[]> searchWithLikes(@Param("q") String q, @Param("userId") Long userId);

    // 2. Optimized List All: Fetches everything in ONE query
    @Query("SELECT s, " +
           "(SELECT COUNT(l) FROM LikeEntity l WHERE l.songId = s.id) AS likeCount, " +
           "(SELECT COUNT(l) > 0 FROM LikeEntity l WHERE l.songId = s.id AND l.userId = :userId) AS isLiked " +
           "FROM Song s")
    List<Object[]> findAllWithLikes(@Param("userId") Long userId);
}
