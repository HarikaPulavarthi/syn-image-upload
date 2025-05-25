package com.synchrony.cloudinary.controller;


import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.ImageRepository;
import com.synchrony.cloudinary.service.CloudinaryService;
import com.synchrony.cloudinary.service.ImageService;
import com.synchrony.cloudinary.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private UserService userService;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ImageService imageService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/upload")
    @Schema(description = "Upload an image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file, Authentication authentication) throws IOException {
        if (file.isEmpty()) {
            log.info("File is empty");
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (!authentication.isAuthenticated()) {
            log.info("User is not authenticated");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("name");

        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            log.info("User not found");
            return ResponseEntity.status(404).body("User not found");
        }
        // Upload the image to Cloudinary
        log.info("Uploading image: {}", file.getOriginalFilename());
        Map result = cloudinaryService.uploadFile(file);
        if (result == null) {
            log.info("Failed to upload image");
            return ResponseEntity.status(500).body("Failed to upload image");
        }
        // Save the image details to the database
        imageService.uploadImage(file,user);
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
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, Authentication authentication) throws IOException {
        if (id == null) {
            log.info("Image ID is empty");
            return ResponseEntity.badRequest().body("Image ID is empty");
        }
        log.info("Deleting image with ID: {}", id);
        Image image = imageRepository.findById(id).orElseThrow();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");
        if (!image.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("You are not authorized to delete this image");
        }
        cloudinaryService.deleteFile(image.getPublicId());
        imageService.deleteImage(id);
        return ResponseEntity.ok("Image deleted successfully");
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images fetched successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "No images found")
    })
    @Schema(description = "Get all images")
    @GetMapping("/myImages")
    public ResponseEntity<?> getMyImages(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("name");
        if (!authentication.isAuthenticated()) {
            log.info("User is not authenticated");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        log.info("Fetching images for user: {}", name);
        // Fetch the images for the authenticated user
        List<Image> images = imageService.getImagesForUser(email);
        if (images == null || images.isEmpty()) {
            log.info("No images found for user: {}", name);
            return ResponseEntity.status(404).body("No images found");
        }
        log.info("Found {} images for user: {}", images.size(), name);
        return ResponseEntity.ok(images);
    }
}
