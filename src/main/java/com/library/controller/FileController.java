package com.library.controller;

import com.library.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload file ảnh
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = fileStorageService.storeCoverImage(file);
            String fileUrl = fileStorageService.getFileUrl(fileName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", fileName);
            response.put("fileUrl", fileUrl);
            response.put("filePath", fileName); // Đường dẫn để điền vào cover_image
            response.put("originalName", file.getOriginalFilename());
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload file: " + ex.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }





    /**
     * Lấy file (download/view)
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = fileStorageService.getContentType(fileName);
        } catch (Exception ex) {
            // Fallback to the default content type if type could not be determined
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Display images inline
        String disposition = "inline";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Lấy file với category và filename
     */
    @GetMapping("/{category}/{fileName:.+}")
    public ResponseEntity<Resource> getFileWithCategory(
            @PathVariable String category, 
            @PathVariable String fileName, 
            HttpServletRequest request) {
        
        String fullFileName = category + "/" + fileName;
        return getFile(fullFileName, request);
    }


}
