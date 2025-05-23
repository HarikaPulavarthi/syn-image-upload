package com.synchrony.cloudinary.service;

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

    public Image uploadImage(MultipartFile file, Principal principal) throws IOException {

        Map result = cloudinaryService.uploadFile(file);

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setPublicId((String) result.get("public_id"));
        image.setUrl((String) result.get("secure_url"));
        image.setUser(user);

        return imageRepository.save(image);
    }

    public String deleteImage(Long imageId, Principal principal) throws IOException {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!image.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }
        log.info("Deleting image with ID: {}", imageId);
        cloudinaryService.deleteFile(image.getPublicId());
        imageRepository.delete(image);
        return "Image deleted successfully";
    }

    public List<Image> getImagesForUser(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        if (principal.getName() == null || principal.getName().isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }
        log.info("Fetching images for user: {}", principal.getName());
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getImages();
    }
}
