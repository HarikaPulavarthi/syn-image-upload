package com.synchrony.cloudinary.repository;

import com.synchrony.cloudinary.entity.Image;
import com.synchrony.cloudinary.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findById_shouldReturnImage_whenImageExists() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        Image image = new Image();
        image.setFilename("test.png");
        image.setPublicId("abc123");
        image.setUrl("http://example.com/image.png");
        image.setUser(user);

        entityManager.persist(user);
        Image savedImage = entityManager.persist(image);
        entityManager.flush();

        Optional<Image> found = imageRepository.findById(savedImage.getId());

        assertTrue(found.isPresent());
        assertEquals("test.png", found.get().getFilename());
    }

    @Test
    void findById_shouldReturnEmpty_whenImageDoesNotExist() {
        Optional<Image> found = imageRepository.findById(999L);
        assertTrue(found.isEmpty());
    }
}
