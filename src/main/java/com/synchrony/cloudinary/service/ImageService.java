package com.synchrony.cloudinary.service;

import com.synchrony.cloudinary.dto.ImageUploadEvent;
import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.ImageRepository;
import com.synchrony.cloudinary.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ImageService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    public Image uploadImage(MultipartFile file, User user) throws IOException {

        Map result = cloudinaryService.uploadFile(file);

        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setPublicId((String) result.get("public_id"));
        image.setUrl((String) result.get("secure_url"));
        image.setUser(user);
        kafkaProducer.sendImageUploadEvent(new ImageUploadEvent(user.getUsername(), image.getFilename()));
        return imageRepository.save(image);
    }

    public String deleteImage(Long imageId) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        log.info("Deleting image with ID: {}", imageId);
        cloudinaryService.deleteFile(image.getPublicId());
        imageRepository.delete(image);
        return "Image deleted successfully";
    }

    public List<Image> getImagesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("Fetching images for user: {}", user.getName());
        return user.getImages();
    }
}

