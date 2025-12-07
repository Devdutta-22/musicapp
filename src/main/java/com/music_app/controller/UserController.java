package com.music_app.controller;

import com.music_app.model.User;
import com.music_app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- THE EVOLUTION ENGINE ---
    @PostMapping("/{id}/add-minutes")
    public ResponseEntity<?> addMinutes(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return userRepository.findById(id).map(user -> {
            
            // 1. Absorb Time
            int newMinutes = 0;
            if (payload.get("minutes") instanceof Number) {
                newMinutes = ((Number) payload.get("minutes")).intValue();
            }
            
            user.setTotalMinutesListened(user.getTotalMinutesListened() + newMinutes);

            // 2. Absorb Elemental Energy (The Genre)
            String genre = (String) payload.get("genre");
            
            if (genre != null) {
                switch (genre) {
                    case "Rock": 
                    case "Metal":
                        user.setMagmaEnergy(user.getMagmaEnergy() + newMinutes);
                        break;
                    case "Pop": 
                    case "Dance":
                        user.setNeonGas(user.getNeonGas() + newMinutes);
                        break;
                    case "Lo-Fi": 
                    case "Classical":
                        user.setPermafrost(user.getPermafrost() + newMinutes);
                        break;
                    case "Hip-Hop": 
                    case "Electronic":
                    case "Rap":
                        user.setLiquidChrome(user.getLiquidChrome() + newMinutes);
                        break;
                }
            }

            // 3. Check for Planetary Evolution
            // We check if they have listened for more than 5 minutes total
            if (user.getTotalMinutesListened() > 5) {
                evolvePlanet(user);
            }

            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "totalMinutes", user.getTotalMinutesListened(),
                "planetType", user.getPlanetType() != null ? user.getPlanetType() : "Nebula",
                "message", "The universe has shifted."
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- THE HYBRID ALGORITHM OF CREATION ---
    private void evolvePlanet(User user) {
        int rock = user.getMagmaEnergy();
        int pop = user.getNeonGas();
        int chill = user.getPermafrost();
        int rap = user.getLiquidChrome();
        
        int totalMusicEnergy = rock + pop + chill + rap;

        // Need a baseline amount of data before determining a type
        if (totalMusicEnergy < 5) return; 

        // Define a threshold: A genre is "strong" if it's > 25% of total listening
        double threshold = totalMusicEnergy * 0.25;

        boolean strongRock = rock > threshold;
        boolean strongPop = pop > threshold;
        boolean strongChill = chill > threshold;
        boolean strongRap = rap > threshold;

        // --- 1. CHECK FOR HYBRIDS (Priority) ---

        // Rock + Pop = Plasma
        if (strongRock && strongPop) {
            user.setPlanetType("Plasma World");
            user.setPlanetName(user.getUsername() + "'s Reactor");
            return;
        }
        // Rock + Chill = Steam
        if (strongRock && strongChill) {
            user.setPlanetType("Steam World");
            user.setPlanetName(user.getUsername() + "'s Geyser");
            return;
        }
        // Pop + Rap = Cyberpunk
        if (strongPop && strongRap) {
            user.setPlanetType("Cyberpunk");
            user.setPlanetName("Neo-" + user.getUsername() + " City");
            return;
        }
        // ... existing hybrids ...

        // Pop (Sun) + Chill (Water) = FOREST (Green)
        if (strongPop && strongChill) {
            user.setPlanetType("Forest World");
            user.setPlanetName("Eden of " + user.getUsername());
            return;
        }

        // ... rest of the code ...

        // --- 2. FALLBACK TO SINGLE DOMINANT TYPE ---
        int max = Math.max(Math.max(rock, pop), Math.max(chill, rap));

        if (max == rock) {
            user.setPlanetType("Volcanic");
            user.setPlanetName(user.getUsername() + "'s Core");
        } else if (max == pop) {
            user.setPlanetType("Gas Giant");
            user.setPlanetName(user.getUsername() + "'s Nebula");
        } else if (max == chill) {
            user.setPlanetType("Ice World");
            user.setPlanetName(user.getUsername() + "'s Tundra");
        } else if (max == rap) {
            user.setPlanetType("Metallic");
            user.setPlanetName("Cyber " + user.getUsername());
        }
    }
}
