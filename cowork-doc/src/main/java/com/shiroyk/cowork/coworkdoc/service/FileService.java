package com.shiroyk.cowork.coworkdoc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class FileService {
    private final Path imageLocation;


    public FileService() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        String path = "dev".equals(profile) ? "./image" : "/home/spring/image";
        this.imageLocation = Paths.get(path).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.imageLocation);
            log.info("Image path: "+ imageLocation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String storeImage(MultipartFile file) {
        String fileName;
        try {
            fileName = DigestUtils.sha256Hex(file.getInputStream());
            Path targetLocation = this.imageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return fileName;
    }

    public Resource loadImageAsResource(String fileName) {
        Path filePath = this.imageLocation.resolve(fileName).normalize();
        try {
            return new UrlResource(filePath.toUri());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
