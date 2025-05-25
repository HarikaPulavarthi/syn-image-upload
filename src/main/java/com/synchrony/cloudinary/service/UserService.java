package com.synchrony.cloudinary.service;

import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Registering user: {}", user.getUsername());
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

   /* public User getUserProfile(String username) {
        log.info("Fetching user profile for username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }*/
}