package com.synchrony.cloudinary.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageUploadEventTest {

    @Test
    void testAllArgsConstructor() {
        ImageUploadEvent event = new ImageUploadEvent("john_doe", "image.jpg");

        assertThat(event.getUsername()).isEqualTo("john_doe");
        assertThat(event.getImageName()).isEqualTo("image.jpg");
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ImageUploadEvent event = new ImageUploadEvent();
        event.setUsername("jane_doe");
        event.setImageName("photo.png");

        assertThat(event.getUsername()).isEqualTo("jane_doe");
        assertThat(event.getImageName()).isEqualTo("photo.png");
    }

    @Test
    void testEqualsAndHashCode() {
        ImageUploadEvent event1 = new ImageUploadEvent("user", "img.png");
        ImageUploadEvent event2 = new ImageUploadEvent("user", "img.png");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void testToString() {
        ImageUploadEvent event = new ImageUploadEvent("user1", "pic.jpg");
        String toString = event.toString();

        assertThat(toString).contains("user1").contains("pic.jpg");
    }
}
