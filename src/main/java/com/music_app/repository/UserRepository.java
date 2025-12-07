package com.music_app.repository;

import com.music_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // This allows us to find a user just by typing their name
    Optional<User> findByUsername(String username);
}
