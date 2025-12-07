package com.music_app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // This creates a table named 'users' in your DB
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    // --- PLANET STATS (For your Astronote feature) ---
    private String planetName;
    private String planetType; // e.g., "Gas Giant", "Volcanic"
    private int totalMinutesListened = 0;

    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPlanetName() { return planetName; }
    public void setPlanetName(String planetName) { this.planetName = planetName; }
    
    public String getPlanetType() { return planetType; }
    public void setPlanetType(String planetType) { this.planetType = planetType; }
    
    public int getTotalMinutesListened() { return totalMinutesListened; }
    public void setTotalMinutesListened(int totalMinutesListened) { this.totalMinutesListened = totalMinutesListened; }
}
