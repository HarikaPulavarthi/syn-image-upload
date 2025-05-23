package com.synchrony.cloudinary.controller;


import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.ImageRepository;
import com.synchrony.cloudinary.repository.UserRepository;
import com.synchrony.cloudinary.service.CloudinaryService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/images/v1")
@Schema(description = "Image API")
public class ImageController {

    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImageRepository imageRepository;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/upload")
    @Schema(description = "Upload an image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file, Principal principal) throws IOException {
        if (file.isEmpty()) {
            log.info("File is empty");
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (principal == null) {
            log.info("User is not authenticated");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        log.info("Uploading image: {}", file.getOriginalFilename());
        // Upload the image to Cloudinary
        Map result = cloudinaryService.uploadFile(file);
        if (result == null) {
            log.info("Failed to upload image");
            return ResponseEntity.status(500).body("Failed to upload image");
        }
        // Save the image details to the database
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        if (user == null) {
            log.info("User not found");
            return ResponseEntity.status(404).body("User not found");
        }
        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setPublicId((String) result.get("public_id"));
        image.setUrl((String) result.get("secure_url"));
        image.setUser(user);
        imageRepository.save(image);
        return ResponseEntity.ok("Image uploaded successfully");
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @Schema(description = "Delete an image")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, Principal principal) throws IOException {
        if (id == null) {
            log.info("Image ID is empty");
            return ResponseEntity.badRequest().body("Image ID is empty");
        }
        log.info("Deleting image with ID: {}", id);
        Image image = imageRepository.findById(id).orElseThrow();
        if (!image.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).body("You are not authorized to delete this image");
        }
        cloudinaryService.deleteFile(image.getPublicId());
        imageRepository.delete(image);
        return ResponseEntity.ok("Image deleted successfully");
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images fetched successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "No images found")
    })
    @Schema(description = "Get all images")
    @GetMapping("/myImages")
    public ResponseEntity<?> getMyImages(Principal principal) {
        if (principal == null) {
            log.info("User is not authenticated");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        log.info("Fetching images for user: {}", principal.getName());
        User user = userRepository.findByUsername(principal.getName()).orElseThrow();
        user.getImages();
        List<Image> images = imageRepository.findByUserId(user.getId());
        if (images.isEmpty()) {
            log.info("No images found for user: {}", principal.getName());
            return ResponseEntity.status(404).body("No images found");
        }
        log.info("Found {} images for user: {}", images.size(), principal.getName());
        return ResponseEntity.ok(images);
    }
}
