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

    // --- NEW: ELEMENTAL ENERGY (For Evolution Logic) ---
    private int magmaEnergy = 0;   // Tracks Rock/Metal
    private int neonGas = 0;       // Tracks Pop/Dance
    private int permafrost = 0;    // Tracks Lo-Fi/Classical
    private int liquidChrome = 0;  // Tracks Hip-Hop/Electronic

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

    // --- NEW GETTERS AND SETTERS ---
    public int getMagmaEnergy() { return magmaEnergy; }
    public void setMagmaEnergy(int magmaEnergy) { this.magmaEnergy = magmaEnergy; }

    public int getNeonGas() { return neonGas; }
    public void setNeonGas(int neonGas) { this.neonGas = neonGas; }

    public int getPermafrost() { return permafrost; }
    public void setPermafrost(int permafrost) { this.permafrost = permafrost; }

    public int getLiquidChrome() { return liquidChrome; }
    public void setLiquidChrome(int liquidChrome) { this.liquidChrome = liquidChrome; }
}
