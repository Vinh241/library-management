package com.library.service;

import com.library.exception.FileNotFoundException;
import com.library.exception.FileStorageException;
import com.library.exception.InvalidFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final List<String> allowedExtensions;
    private final long maxFileSize;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir,
                             @Value("${file.allowed-extensions}") String allowedExtensions,
                             @Value("${file.max-file-size}") long maxFileSize) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = Arrays.asList(allowedExtensions.toLowerCase().split(","));
        this.maxFileSize = maxFileSize;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Lưu file và trả về tên file đã được lưu
     */
    public String storeFile(MultipartFile file, String category) {
        // Validate file
        validateFile(file);

        // Tạo tên file unique
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        try {
            // Tạo thư mục con theo category nếu cần
            Path categoryPath = this.fileStorageLocation.resolve(category);
            Files.createDirectories(categoryPath);
            
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = categoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return category + "/" + fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Lưu file ảnh bìa sách
     */
    public String storeCoverImage(MultipartFile file) {
        return storeFile(file, "covers");
    }

    /**
     * Lấy file dưới dạng Resource
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }

    /**
     * Xóa file
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    /**
     * Kiểm tra file có tồn tại không
     */
    public boolean fileExists(String fileName) {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    /**
     * Lấy đường dẫn đầy đủ của file
     */
    public String getFileUrl(String fileName) {
        return "/api/files/" + fileName;
    }

    /**
     * Validate file upload (chỉ cho phép ảnh cho cover image)
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException("File size exceeds maximum allowed size of " + maxFileSize + " bytes.");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        String fileExtension = getFileExtension(fileName);
        // Chỉ cho phép các định dạng ảnh cho cover image
        List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
        if (!imageExtensions.contains(fileExtension.toLowerCase())) {
            throw new InvalidFileTypeException("Only image files are allowed. Supported types: " + String.join(", ", imageExtensions));
        }
    }

    /**
     * Tạo tên file unique
     */
    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        
        // Làm sạch tên file
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return baseName + "_" + UUID.randomUUID().toString() + "." + fileExtension;
    }

    /**
     * Lấy phần mở rộng của file
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * Lấy MIME type của file
     */
    public String getContentType(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.probeContentType(filePath);
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }
}
