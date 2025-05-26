package com.synchrony.cloudinary.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testUser", "password123", "Test Name", "test@example.com");
    }

    @Test
    void testConstructorAndGetters() {
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getName()).isEqualTo("Test Name");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testSetters() {
        user.setUsername("newUser");
        user.setPassword("newPass");
        user.setName("New Name");
        user.setEmail("new@example.com");

        assertThat(user.getUsername()).isEqualTo("newUser");
        assertThat(user.getPassword()).isEqualTo("newPass");
        assertThat(user.getName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void testImageListAssignment() {
        Image image1 = new Image();
        image1.setFilename("Image1");
        Image image2 = new Image();
        image2.setFilename("Image2");

        user.setImages(List.of(image1, image2));

        assertThat(user.getImages()).hasSize(2);
        assertThat(user.getImages()).extracting(Image::getFilename)
                .containsExactly("Image1", "Image2");
    }

    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertThat(emptyUser).isNotNull();
    }
}
