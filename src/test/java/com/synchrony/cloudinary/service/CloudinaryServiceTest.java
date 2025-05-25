package com.synchrony.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    static class TestableCloudinaryService extends CloudinaryService {
        TestableCloudinaryService(Cloudinary mockCloudinary) {
            super("test", "test", "test");
            try {
                java.lang.reflect.Field cloudinaryField = CloudinaryService.class.getDeclaredField("cloudinary");
                cloudinaryField.setAccessible(true);
                cloudinaryField.set(this, mockCloudinary);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private CloudinaryService createServiceWithMocks() {
        return new TestableCloudinaryService(cloudinary);
    }

    @Test
    void testUploadFile_success() throws IOException {
        CloudinaryService service = createServiceWithMocks();

        MultipartFile file = new MockMultipartFile("file", "hello.png", "image/png", "dummy data".getBytes());

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("public_id", "abc123");
        mockResponse.put("secure_url", "https://example.com/abc123.png");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResponse);

        Map result = service.uploadFile(file);

        assertNotNull(result);
        assertEquals("abc123", result.get("public_id"));
        assertEquals("https://example.com/abc123.png", result.get("secure_url"));

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadFile_emptyFile() throws IOException {
        CloudinaryService service = createServiceWithMocks();

        MultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        Map result = service.uploadFile(emptyFile);

        assertNull(result);
        verify(cloudinary, never()).uploader();
    }

    @Test
    void testDeleteFile_success() throws IOException {
        CloudinaryService service = createServiceWithMocks();

        when(cloudinary.uploader()).thenReturn(uploader);

        service.deleteFile("public-id-123");

        verify(uploader).destroy(eq("public-id-123"), anyMap());
    }

    @Test
    void testDeleteFile_emptyPublicId() throws IOException {
        CloudinaryService service = createServiceWithMocks();
        when(cloudinary.uploader()).thenReturn(uploader);

        service.deleteFile("");

        verify(uploader).destroy(eq(""), anyMap());
    }

    @Test
    void testDeleteFile_nullPublicId() throws IOException {
        CloudinaryService service = createServiceWithMocks();

        when(cloudinary.uploader()).thenReturn(uploader);

        service.deleteFile(null);

        verify(uploader).destroy(eq(null), anyMap());
    }
}
