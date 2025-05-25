package com.synchrony.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public Map uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.info("File is empty");
            return null;
        }
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }

    public void deleteFile(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            log.info("Public ID is empty");
        }
        log.info("Deleting file with public ID: {}", publicId);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
