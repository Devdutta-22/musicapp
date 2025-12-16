package com.music_app.repository;

import com.music_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // --- NEW: LEADERBOARD QUERY ---
    @Query("SELECT u FROM User u ORDER BY u.totalMinutesListened DESC LIMIT 50")
    List<User> findTopListeners();
}
