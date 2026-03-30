package com.classified.app.service;

import com.classified.app.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException ex) {
            log.error("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file");
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String fileName = UUID.randomUUID() + extension;
        try {
            if (fileName.contains("..")) {
                throw new BadRequestException("Invalid filename: " + fileName);
            }
            Path targetLocation = this.uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file: " + ex.getMessage());
        }
    }

    public List<String> storeFiles(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                paths.add(storeFile(file));
            }
        }
        return paths;
    }

    public void deleteFile(String filePath) {
        try {
            String fileName = filePath.replace("/uploads/", "");
            Path path = this.uploadPath.resolve(fileName);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", filePath, ex);
        }
    }
}
