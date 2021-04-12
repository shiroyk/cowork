package com.shiroyk.cowork.coworkdoc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileService {
    private final Path imageLocation;


    public FileService() {
        this.imageLocation = Paths.get("./image").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.imageLocation);
            log.info("Image path: "+imageLocation.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String storeImage(MultipartFile file) {
        String fileName = UUID.randomUUID().toString();
        try {
            Path targetLocation = this.imageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return fileName;
    }

    public Resource loadImageAsResource(String fileName) throws IOException {
        Path filePath = this.imageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if(resource.exists()) {
            return resource;
        } else {
            return null;
        }
    }
}
