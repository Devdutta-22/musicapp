// package com.music_app.controller;

// import com.music_app.model.Song;
// import com.music_app.repository.SongRepository;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.io.InputStreamResource;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException;

// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// @RestController
// @RequestMapping("/api/audio")
// public class AudioStreamController {

//     private final SongRepository songRepository;

//     @Value("${app.media.base-path}")
//     private String mediaBasePath;

//     public AudioStreamController(SongRepository songRepository) {
//         this.songRepository = songRepository;
//     }

//     /**
//      * Stream a song by ID. Supports Range headers for seeking.
//      * song.getFilePath() can be:
//      *  - an absolute path (starts with "/" on Linux/mac or drive letter on Windows) -> used directly
//      *  - a relative filename (e.g. "track1.mp3") -> resolved against app.media.base-path
//      */
//     @GetMapping("/{songId}")
//     public ResponseEntity<InputStreamResource> streamAudio(
//             @PathVariable Long songId,
//             @RequestHeader(value = "Range", required = false) String rangeHeader) {

//         Song song = songRepository.findById(songId)
//                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found"));

//         // Determine actual filesystem path
//         Path filePath = resolveSongFilePath(song.getFilePath());
//         if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found: " + filePath);
//         }

//         try {
//             long fileSize = Files.size(filePath);
//             String contentType = song.getMimeType();
//             if (contentType == null || contentType.isBlank()) {
//                 contentType = Files.probeContentType(filePath);
//                 if (contentType == null) contentType = "application/octet-stream";
//             }

//             // No Range header -> return full file (200)
//             if (rangeHeader == null) {
//                 InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
//                 return ResponseEntity.ok()
//                         .contentType(MediaType.parseMediaType(contentType))
//                         .contentLength(fileSize)
//                         .header(HttpHeaders.ACCEPT_RANGES, "bytes")
//                         .body(resource);
//             }

//             // Parse Range header, example: "bytes=0-"
//             Pattern pattern = Pattern.compile("bytes=(\\d*)-(\\d*)");
//             Matcher matcher = pattern.matcher(rangeHeader);
//             if (!matcher.matches()) {
//                 throw new ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Invalid Range header");
//             }

//             String startGroup = matcher.group(1);
//             String endGroup = matcher.group(2);
//             long start = startGroup.isEmpty() ? 0 : Long.parseLong(startGroup);
//             long end = endGroup.isEmpty() ? fileSize - 1 : Long.parseLong(endGroup);

//             if (end >= fileSize) end = fileSize - 1;
//             if (start > end) {
//                 throw new ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Range start > end");
//             }

//             long contentLength = end - start + 1;

//             InputStream inputStream = Files.newInputStream(filePath);
//             // skip to start
//             long skipped = inputStream.skip(start);
//             if (skipped < start) {
//                 // couldn't skip properly
//                 inputStream.close();
//                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to seek file stream");
//             }

//             InputStreamResource resource = new InputStreamResource(new LimitedInputStream(inputStream, contentLength));

//             HttpHeaders headers = new HttpHeaders();
//             headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
//             headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
//             headers.setContentLength(contentLength);

//             return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
//                     .headers(headers)
//                     .contentType(MediaType.parseMediaType(contentType))
//                     .body(resource);

//         } catch (IOException e) {
//             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File access error", e);
//         }
//     }

//     private Path resolveSongFilePath(String storedPath) {
//         if (storedPath == null) throw new IllegalArgumentException("song.filePath is null");
//         Path p = Paths.get(storedPath);
//         // If path is absolute, return it directly
//         if (p.isAbsolute()) {
//             return p.normalize();
//         }
//         // On Windows the storedPath might start with a drive letter; Paths.get handles that as absolute.
//         // Otherwise resolve against configured media base path
//         return Paths.get(mediaBasePath).resolve(storedPath).normalize();
//     }

//     // Inner helper to limit reads to exactly 'left' bytes
//     private static class LimitedInputStream extends InputStream {
//         private final InputStream in;
//         private long left;
//         public LimitedInputStream(InputStream in, long left) {
//             this.in = in;
//             this.left = left;
//         }
//         @Override
//         public int read() throws IOException {
//             if (left <= 0) return -1;
//             int r = in.read();
//             if (r != -1) left--;
//             return r;
//         }
//         @Override
//         public int read(byte[] b, int off, int len) throws IOException {
//             if (left <= 0) return -1;
//             int toRead = (int) Math.min(len, left);
//             int r = in.read(b, off, toRead);
//             if (r != -1) left -= r;
//             return r;
//         }
//         @Override
//         public void close() throws IOException { in.close(); }
//     }
// }
