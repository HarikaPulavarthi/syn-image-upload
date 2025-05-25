
package com.synchrony.cloudinary.controller;

import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.ImageRepository;
import com.synchrony.cloudinary.service.CloudinaryService;
import com.synchrony.cloudinary.service.ImageService;
import com.synchrony.cloudinary.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ImageControllerTest {

    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private UserService userService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Authentication authentication;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    @Test
    void uploadImage_whenFileIsEmpty_shouldReturnBadRequest() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(true);

        ResponseEntity<?> response = imageController.uploadImage(multipartFile, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File is empty", response.getBody());
    }

    @Test
    void uploadImage_whenUserNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> response = imageController.uploadImage(multipartFile, authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not authenticated", response.getBody());
    }

    @Test
    void uploadImage_whenUserNotFound_shouldReturnNotFound() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(authentication.isAuthenticated()).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@gmail.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);

        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = imageController.uploadImage(multipartFile, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void uploadImage_whenCloudinaryUploadFails_shouldReturnServerError() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(authentication.isAuthenticated()).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@gmail.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);

        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        when(userService.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadFile(multipartFile)).thenReturn(null);

        ResponseEntity<?> response = imageController.uploadImage(multipartFile, authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to upload image", response.getBody());
    }

    @Test
    void uploadImage_whenSuccess_shouldReturnOk() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(authentication.isAuthenticated()).thenReturn(true);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);

        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setName("Test User");
        user.setEmail("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Map<String, String> cloudinaryResponse = new HashMap<>();
        cloudinaryResponse.put("public_id", "123");
        cloudinaryResponse.put("secure_url", "http://example.com/image.jpg");
        when(cloudinaryService.uploadFile(multipartFile)).thenReturn(cloudinaryResponse);

        ResponseEntity<?> response = imageController.uploadImage(multipartFile, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Image uploaded successfully", response.getBody());
    }

    @Test
    void deleteImage_whenIdIsNull_shouldReturnBadRequest() throws Exception {
        ResponseEntity<?> response = imageController.deleteImage(null, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Image ID is empty", response.getBody());
    }

    @Test
    void deleteImage_whenImageNotFound_shouldThrowException() {
        Long imageId = 1L;
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        Assert.assertThrows(NoSuchElementException.class, () -> {
            imageController.deleteImage(imageId, authentication);
        });
    }

    @Test
    void deleteImage_whenUserIsNotOwner_shouldReturnForbidden() throws IOException {
        Long imageId = 1L;
        Image image = new Image();
        User owner = new User();
        owner.setEmail("test@gmail.com");
        image.setUser(owner);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test1@gmail.com");
        when(authentication.getPrincipal()).thenReturn(jwt);

        ResponseEntity<?> response = imageController.deleteImage(imageId, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not authorized to delete this image", response.getBody());
    }

    @Test
    void deleteImage_whenValidRequest_shouldReturnOk() throws IOException {
        Long imageId = 1L;
        Image image = new Image();
        image.setPublicId("some-public-id");
        User user = new User();
        user.setEmail("test@example.com");
        image.setUser(user);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(authentication.getPrincipal()).thenReturn(jwt);

        doNothing().when(cloudinaryService).deleteFile("some-public-id");
        when(imageService.deleteImage(imageId)).thenReturn("Image deleted successfully");

        ResponseEntity<?> response = imageController.deleteImage(imageId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Image deleted successfully", response.getBody());
    }

    @Test
    void getMyImages_whenUserIsAuthenticatedAndImagesExist_shouldReturnImages() {
        // Mock JWT and authentication
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(authentication.isAuthenticated()).thenReturn(true);

        List<Image> mockImages = List.of(new Image(), new Image());
        when(imageService.getImagesForUser("test@example.com")).thenReturn(mockImages);

        ResponseEntity<?> response = imageController.getMyImages(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockImages, response.getBody());
    }

    @Test
    void getMyImages_whenUserIsAuthenticatedButNoImages_shouldReturn404() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(imageService.getImagesForUser("test@example.com")).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = imageController.getMyImages(authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No images found", response.getBody());
    }

    @Test
    void getMyImages_whenAuthenticationIsInvalid_shouldReturn401() {
        when(authentication.isAuthenticated()).thenReturn(false);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("email")).thenReturn("test@example.com");
        when(jwt.getClaim("name")).thenReturn("Test User");
        when(authentication.getPrincipal()).thenReturn(jwt);

        ResponseEntity<?> response = imageController.getMyImages(authentication);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not authenticated", response.getBody());
    }
}