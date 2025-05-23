package com.synchrony.cloudinary.controller;


import com.synchrony.cloudinary.entity.User;
import com.synchrony.cloudinary.repository.UserRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Schema(description = "User API")
@RequestMapping("/api/users/v1")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User Registered"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            log.info("Username or password is empty");
            return ResponseEntity.badRequest().body(null);
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            log.info("Username already exists");
            return ResponseEntity.status(409).body(null);
        }
        if (user.getName() == null || user.getEmail() == null) {
            log.info("Name or email is empty");
            return ResponseEntity.badRequest().body(null);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Registering user: {}", user.getUsername());
        userRepository.save(user);
        return ResponseEntity.status(201).body(user);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        log.info("Fetching user: {}", username);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.info("User not found");
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(user);
    }
}
