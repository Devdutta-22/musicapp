package com.music_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Enable CORS in Spring Security
            .cors(Customizer.withDefaults())
            // 2. Disable CSRF (often causes 403s for POST/PUT requests in APIs)
            .csrf(csrf -> csrf.disable())
            // 3. Allow access to your API endpoints without login
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**", "/media/**").permitAll() // Allow your specific endpoints
                .anyRequest().authenticated() // Secure everything else
            );

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow credentials (cookies, auth headers)
        config.setAllowCredentials(true);
        
        // Allow your Specific Frontend Origins
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "https://musicapp-frontend-devduttas-projects.vercel.app" // Your Vercel URL
        ));
        
        // Allow all headers and methods
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
