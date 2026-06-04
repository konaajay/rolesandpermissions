package com.project.www.controller;

import com.project.www.enums.*;

import com.project.www.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/marketing/media")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MediaController {

    private final FileStorageService fileStorageService;

    // ✅ Upload
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('MARKETING_MEDIA_UPLOAD')")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {

        // 🔒 Validation
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // 🔒 Type check
        List<String> allowedTypes = List.of("image/png", "image/jpeg", "image/gif", "image/svg+xml");
        if (!allowedTypes.contains(file.getContentType())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type"));
        }

        String fileName = fileStorageService.storeFile(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/marketing/media/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "fileUrl", fileUrl));
    }

    // ✅ Download
    @GetMapping("/{fileName:.+}")
    @PreAuthorize("hasAuthority('MARKETING_MEDIA_VIEW')")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {

        try {
            Path filePath = fileStorageService.loadFileAsPath(fileName);
            Resource resource = new UrlResource(Objects.requireNonNull(filePath.toUri()));

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = resolveContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Clean helper
    private String resolveContentType(String fileName) {
        if (fileName.endsWith(".png"))
            return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return "image/jpeg";
        if (fileName.endsWith(".gif"))
            return "image/gif";
        if (fileName.endsWith(".svg"))
            return "image/svg+xml";
        return "application/octet-stream";
    }
}
