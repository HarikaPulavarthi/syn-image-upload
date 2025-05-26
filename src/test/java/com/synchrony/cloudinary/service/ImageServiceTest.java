package com.synchrony.cloudinary.service;

import com.synchrony.cloudinary.dto.ImageUploadEvent;
import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.ImageRepository;
import com.synchrony.cloudinary.repository.UserRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImageService imageService;

    @Mock
    private KafkaProducer kafkaProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadImage_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();
        user.setEmail("test@example.com");

        when(file.getOriginalFilename()).thenReturn("file.jpg");
        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("public_id", "pid");
        uploadResult.put("secure_url", "http://url");
        when(cloudinaryService.uploadFile(file)).thenReturn(uploadResult);

        Image savedImage = new Image();
        savedImage.setFilename("file.jpg");
        savedImage.setPublicId("pid");
        savedImage.setUrl("http://url");
        savedImage.setUser(user);

        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        doNothing().when(kafkaProducer).sendImageUploadEvent(any(ImageUploadEvent.class));
        Image result = imageService.uploadImage(file, user);

        assertNotNull(result);
        assertEquals("file.jpg", result.getFilename());
        assertEquals("pid", result.getPublicId());
        assertEquals("http://url", result.getUrl());
        assertEquals(user, result.getUser());
    }

    @Test
    void uploadImage_cloudinaryFailure() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        User user = new User();
        when(cloudinaryService.uploadFile(file)).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> imageService.uploadImage(file, user));
    }

    @Test
    void deleteImage_success() throws IOException {
        Image image = new Image();
        image.setId(1L);
        image.setPublicId("pid");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        String result = imageService.deleteImage(1L);

        assertEquals("Image deleted successfully", result);
        verify(cloudinaryService, times(1)).deleteFile("pid");
        verify(imageRepository, times(1)).delete(image);
    }

    @Test
    void deleteImage_notFound() {
        when(imageRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> imageService.deleteImage(2L));
    }

    @Test
    void getImagesForUser_found() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        Image image = new Image();
        image.setUser(user);
        List<Image> images = List.of(image);
        user.setImages(images);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        List<Image> result = imageService.getImagesForUser(email);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user, result.get(0).getUser());
    }

    @Test
    void getImagesForUser_userNotFound() {
        when(userRepository.findByEmail("nouser@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> imageService.getImagesForUser("nouser@example.com"));
    }
}