// package com.music_app.controller;

// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.io.InputStreamResource;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException;

// import java.io.IOException;
// import java.nio.file.*;

// @RestController
// public class CoverController {

//     // fallback default if you don't set this in application.properties
//     @Value("${app.media.base-path:C:/music_files}")
//     private String mediaBasePath;

//     /**
//      * Serve nested paths like:
//      *   /api/cover/covers/Arijit.png
//      *   /api/cover/artists/whatever.jpg
//      *
//      * This method extracts the part after /api/cover/ and resolves it
//      * relative to mediaBasePath. It prevents path traversal.
//      */
//     @GetMapping("/api/cover/**")
//     public ResponseEntity<InputStreamResource> serveCover(HttpServletRequest request) {
//         try {
//             String requestUri = request.getRequestURI();                 // e.g. /api/cover/covers/Arijit.png
//             String relative = requestUri.replaceFirst("/api/cover/", ""); // e.g. covers/Arijit.png

//             Path base = Paths.get(mediaBasePath).toAbsolutePath().normalize();
//             Path file = base.resolve(relative).normalize();

//             // prevent path traversal outside base
//             if (!file.startsWith(base)) {
//                 return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//             }
//             if (!Files.exists(file) || !Files.isReadable(file)) {
//                 return ResponseEntity.notFound().build();
//             }

//             String contentType = Files.probeContentType(file);
//             if (contentType == null) contentType = "application/octet-stream";

//             InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
//             return ResponseEntity.ok()
//                     .contentLength(Files.size(file))
//                     .contentType(MediaType.parseMediaType(contentType))
//                     .body(resource);

//         } catch (IOException e) {
//             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File read error", e);
//         }
//     }
// }
