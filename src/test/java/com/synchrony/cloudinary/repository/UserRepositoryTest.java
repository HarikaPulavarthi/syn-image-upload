package com.synchrony.cloudinary.repository;

import com.synchrony.cloudinary.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUsername_shouldReturnUser_whenUserExists() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("test_user");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUserDoesNotExist() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("test_user1");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserExists() {

        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test_user", found.get().getUsername());
    }

    @Test
    void findByEmail_shouldReturnUser_whenUserNotExists() {

        User user = new User();
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("test1@example.com");

        assertFalse(found.isPresent());
    }
}